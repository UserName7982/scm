package com.scm.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scm.entity.ContactGroup;
import com.scm.entity.User;
import com.scm.helper.ResourcesNotFound;
import com.scm.repositories.contactrepo;
import com.scm.repositories.grouprepo;

@Service
public class GroupService {

    public static final int MAX_NAME = 50;

    @Autowired
    private grouprepo groups;

    @Autowired
    private contactrepo contacts;

    public List<ContactGroup> list(User user) {
        return groups.findByUserOrderByNameAsc(user);
    }

    /** Returns the group only if it belongs to this user, otherwise 404. */
    public ContactGroup getOwned(String id, User user) {
        ContactGroup g = groups.findById(id).orElseThrow(() -> new ResourcesNotFound("Group not found"));
        if (g.getUser() == null || !g.getUser().getUserid().equals(user.getUserid())) {
            throw new ResourcesNotFound("Group not found");
        }
        return g;
    }

    public boolean nameTaken(User user, String name) {
        return groups.existsByUserAndNameIgnoreCase(user, name);
    }

    public ContactGroup create(User user, String name) {
        ContactGroup g = new ContactGroup();
        g.setId(UUID.randomUUID().toString());
        g.setName(name);
        g.setUser(user);
        return groups.save(g);
    }

    public ContactGroup rename(ContactGroup g, String name) {
        g.setName(name);
        return groups.save(g);
    }

    public ContactGroup findOrCreate(User user, String name) {
        return groups.findByUserAndNameIgnoreCase(user, name).orElseGet(() -> create(user, name));
    }

    public long countContacts(ContactGroup g) {
        return contacts.countByContactGroup(g);
    }

    /** Deleting a group keeps its contacts; they just become ungrouped. */
    @Transactional
    public void delete(ContactGroup g) {
        contacts.clearGroup(g);
        groups.delete(g);
    }

    /** Used when an account is deleted. */
    @Transactional
    public void deleteAllForUser(User user) {
        contacts.clearGroupsForUser(user);
        groups.deleteAllByUser(user);
    }
}
