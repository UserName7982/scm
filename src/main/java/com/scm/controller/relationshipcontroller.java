package com.scm.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.Services.RelationshipService;
import com.scm.Services.services;
import com.scm.entity.Contact;
import com.scm.entity.InteractionType;
import com.scm.entity.User;
import com.scm.helper.Helper;
import com.scm.helper.Message;
import com.scm.helper.MessageType;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class relationshipcontroller {

    @Autowired
    private RelationshipService relationships;

    @Autowired
    private services uServices;

    private User currentUser(Authentication authentication) {
        return uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
    }

    private void flash(HttpSession session, String text, MessageType type) {
        session.setAttribute("message", Message.builder().content(text).messageType(type).build());
    }

    private static Map<String, Object> section(String key, String title, String border, List<Contact> contacts) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("key", key);
        m.put("title", title);
        m.put("border", border);
        m.put("contacts", contacts);
        return m;
    }

    private static InteractionType parseType(String raw, InteractionType fallback) {
        try {
            return InteractionType.valueOf(raw);
        } catch (Exception e) {
            return fallback;
        }
    }

    // ------------------------------------------------------------ timeline (per contact)

    @GetMapping("/timeline/{contactId}")
    public String timeline(@PathVariable("contactId") String contactId, Model model, Authentication authentication) {
        Contact contact = relationships.getOwnedContact(contactId, currentUser(authentication));
        model.addAttribute("contact", contact);
        model.addAttribute("interactions", relationships.timeline(contact));
        model.addAttribute("types", InteractionType.values());
        model.addAttribute("today", relationships.today());
        return "user/timeline";
    }

    @PostMapping("/timeline/{contactId}/log")
    public String log(@PathVariable("contactId") String contactId,
            @RequestParam("type") String type,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication, HttpSession session) {
        Contact contact = relationships.getOwnedContact(contactId, currentUser(authentication));
        relationships.log(contact, parseType(type, InteractionType.OTHER), note, date);
        flash(session, "Interaction logged.", MessageType.green);
        return "redirect:/user/timeline/" + contactId;
    }

    @PostMapping("/timeline/{contactId}/delete/{interactionId}")
    public String deleteEntry(@PathVariable("contactId") String contactId,
            @PathVariable("interactionId") String interactionId, Authentication authentication, HttpSession session) {
        Contact contact = relationships.getOwnedContact(contactId, currentUser(authentication));
        relationships.deleteInteraction(contact, interactionId);
        flash(session, "Entry removed.", MessageType.green);
        return "redirect:/user/timeline/" + contactId;
    }

    @PostMapping("/timeline/{contactId}/followup")
    public String setFollowUp(@PathVariable("contactId") String contactId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "note", required = false) String note,
            Authentication authentication, HttpSession session) {
        Contact contact = relationships.getOwnedContact(contactId, currentUser(authentication));
        LocalDate today = relationships.today();
        if (date.isBefore(today) || date.isAfter(today.plusYears(5))) {
            flash(session, "Pick a follow-up date from today up to 5 years ahead.", MessageType.red);
        } else {
            relationships.setFollowUp(contact, date, note);
            flash(session, "Follow-up reminder saved.", MessageType.green);
        }
        return "redirect:/user/timeline/" + contactId;
    }

    @PostMapping("/timeline/{contactId}/followup/clear")
    public String clearFollowUp(@PathVariable("contactId") String contactId, Authentication authentication,
            HttpSession session) {
        Contact contact = relationships.getOwnedContact(contactId, currentUser(authentication));
        relationships.clearFollowUp(contact);
        flash(session, "Follow-up reminder removed.", MessageType.green);
        return "redirect:/user/timeline/" + contactId;
    }

    // ------------------------------------------------------------ reminders page

    @GetMapping("/reminders")
    public String reminders(Model model, Authentication authentication) {
        User user = currentUser(authentication);
        LocalDate today = relationships.today();

        List<Contact> due = relationships.dueOrOverdue(user);
        List<Contact> overdue = due.stream().filter(c -> c.getFollowUpDate().isBefore(today)).toList();
        List<Contact> dueToday = due.stream().filter(c -> c.getFollowUpDate().equals(today)).toList();
        List<Contact> cold = relationships.goingCold(user, 10);

        Map<String, Long> daysOverdue = new HashMap<>();
        for (Contact c : overdue) {
            daysOverdue.put(c.getId(), ChronoUnit.DAYS.between(c.getFollowUpDate(), today));
        }
        Map<String, Long> daysSince = new HashMap<>();
        for (Contact c : cold) {
            daysSince.put(c.getId(), ChronoUnit.DAYS.between(c.getLastContactedAt().toLocalDate(), today));
        }

        List<Contact> upcoming = relationships.upcoming(user, 7);
        List<Map<String, Object>> sections = new ArrayList<>();
        sections.add(section("overdue", "Overdue", "border-red-500", overdue));
        sections.add(section("today", "Due today", "border-orange-500", dueToday));
        sections.add(section("upcoming", "Coming up (next 7 days)", "border-blue-500", upcoming));
        sections.add(section("cold", "Going cold (no contact for " + RelationshipService.COLD_DAYS + "+ days)", "border-gray-400", cold));
        model.addAttribute("sections", sections);
        model.addAttribute("nothing", overdue.isEmpty() && dueToday.isEmpty() && upcoming.isEmpty() && cold.isEmpty());
        model.addAttribute("daysOverdue", daysOverdue);
        model.addAttribute("daysSince", daysSince);
        model.addAttribute("types", InteractionType.values());
        return "user/reminders";
    }

    @PostMapping("/reminders/done/{contactId}")
    public String done(@PathVariable("contactId") String contactId,
            @RequestParam(value = "type", defaultValue = "CALL") String type,
            Authentication authentication, HttpSession session) {
        Contact contact = relationships.getOwnedContact(contactId, currentUser(authentication));
        relationships.completeFollowUp(contact, parseType(type, InteractionType.CALL));
        flash(session, "Nice! Follow-up logged and cleared.", MessageType.green);
        return "redirect:/user/reminders";
    }

    @PostMapping("/reminders/snooze/{contactId}")
    public String snooze(@PathVariable("contactId") String contactId, Authentication authentication,
            HttpSession session) {
        Contact contact = relationships.getOwnedContact(contactId, currentUser(authentication));
        relationships.snooze(contact, 7);
        flash(session, "Snoozed for 7 days.", MessageType.green);
        return "redirect:/user/reminders";
    }
}
