package com.scm.Services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scm.entity.Contact;
import com.scm.entity.Interaction;
import com.scm.entity.InteractionType;
import com.scm.entity.User;
import com.scm.helper.ResourcesNotFound;
import com.scm.repositories.contactrepo;
import com.scm.repositories.interactionrepo;

/** "Stay in touch": interaction log, follow-up reminders and "going cold" detection. */
@Service
public class RelationshipService {

    public static final int COLD_DAYS = 30;
    public static final int MAX_NOTE = 500;

    /** Reminders are day-based, so "today" must use the user's time zone, not the server's. */
    @Value("${app.timezone:Asia/Kolkata}")
    private String timezone;

    @Autowired
    private contactrepo contacts;

    @Autowired
    private interactionrepo interactions;

    public LocalDate today() {
        return LocalDate.now(ZoneId.of(timezone));
    }

    public LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of(timezone));
    }

    /** Returns the contact only if it belongs to this user, otherwise 404. */
    public Contact getOwnedContact(String id, User user) {
        Contact c = contacts.findById(id).orElseThrow(() -> new ResourcesNotFound("Contact not found"));
        if (c.getUser() == null || !c.getUser().getUserid().equals(user.getUserid())) {
            throw new ResourcesNotFound("Contact not found");
        }
        return c;
    }

    public List<Interaction> timeline(Contact contact) {
        return interactions.findByContactOrderByOccurredAtDesc(contact);
    }

    @Transactional
    public Interaction log(Contact contact, InteractionType type, String note, LocalDate date) {
        LocalDate today = today();
        LocalDate day = (date == null || date.isAfter(today)) ? today : date;
        LocalDateTime when = day.equals(today) ? now() : day.atTime(LocalTime.NOON);

        Interaction i = new Interaction();
        i.setId(UUID.randomUUID().toString());
        i.setContact(contact);
        i.setType(type);
        i.setNote(clean(note, MAX_NOTE));
        i.setOccurredAt(when);
        interactions.save(i);

        if (contact.getLastContactedAt() == null || when.isAfter(contact.getLastContactedAt())) {
            contact.setLastContactedAt(when);
            contacts.save(contact);
        }
        return i;
    }

    @Transactional
    public void deleteInteraction(Contact contact, String interactionId) {
        Interaction i = interactions.findById(interactionId).orElseThrow(() -> new ResourcesNotFound("Entry not found"));
        if (i.getContact() == null || !i.getContact().getId().equals(contact.getId())) {
            throw new ResourcesNotFound("Entry not found");
        }
        interactions.delete(i);
        interactions.flush();
        contact.setLastContactedAt(
                interactions.findFirstByContactOrderByOccurredAtDesc(contact).map(Interaction::getOccurredAt).orElse(null));
        contacts.save(contact);
    }

    public void setFollowUp(Contact contact, LocalDate date, String note) {
        contact.setFollowUpDate(date);
        contact.setFollowUpNote(clean(note, 200));
        contacts.save(contact);
    }

    public void clearFollowUp(Contact contact) {
        contact.setFollowUpDate(null);
        contact.setFollowUpNote(null);
        contacts.save(contact);
    }

    /** Marks a follow-up as done: logs the interaction and clears the reminder. */
    @Transactional
    public void completeFollowUp(Contact contact, InteractionType type) {
        String why = contact.getFollowUpNote();
        log(contact, type, why == null || why.isBlank() ? "Follow-up completed" : "Follow-up: " + why, today());
        clearFollowUp(contact);
    }

    public void snooze(Contact contact, int days) {
        contact.setFollowUpDate(today().plusDays(days));
        contacts.save(contact);
    }

    // ------------------------------------------------------------ lists for pages

    /** Follow-ups due today or overdue, oldest first. */
    public List<Contact> dueOrOverdue(User user) {
        return contacts.findByUserAndFollowUpDateLessThanEqualOrderByFollowUpDateAsc(user, today());
    }

    public long dueCount(User user) {
        return contacts.countByUserAndFollowUpDateLessThanEqual(user, today());
    }

    public List<Contact> upcoming(User user, int days) {
        return contacts.findByUserAndFollowUpDateBetweenOrderByFollowUpDateAsc(user, today().plusDays(1), today().plusDays(days));
    }

    /** Contacts last spoken to more than COLD_DAYS ago that have no reminder set. */
    public List<Contact> goingCold(User user, int limit) {
        LocalDateTime cutoff = now().minusDays(COLD_DAYS);
        return contacts.findByUserAndLastContactedAtBeforeOrderByLastContactedAtAsc(user, cutoff, PageRequest.of(0, 50))
                .stream().filter(c -> c.getFollowUpDate() == null).limit(limit).toList();
    }

    private static String clean(String text, int max) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String t = text.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }
}
