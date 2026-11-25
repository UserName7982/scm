package com.scm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.Services.GroupService;
import com.scm.Services.services;
import com.scm.entity.ContactGroup;
import com.scm.entity.User;
import com.scm.helper.Helper;
import com.scm.helper.Message;
import com.scm.helper.MessageType;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/groups")
public class groupcontroller {

    @Autowired
    private GroupService groupService;

    @Autowired
    private services uServices;

    private User currentUser(Authentication authentication) {
        return uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
    }

    private String flash(HttpSession session, String text, MessageType type) {
        session.setAttribute("message", Message.builder().content(text).messageType(type).build());
        return "redirect:/user/groups";
    }

    @GetMapping
    public String list(Model model, Authentication authentication) {
        User user = currentUser(authentication);
        List<ContactGroup> groups = groupService.list(user);
        Map<String, Long> counts = new HashMap<>();
        for (ContactGroup g : groups) {
            counts.put(g.getId(), groupService.countContacts(g));
        }
        model.addAttribute("groups", groups);
        model.addAttribute("counts", counts);
        return "user/groups";
    }

    @PostMapping("/add")
    public String add(@RequestParam("name") String name, Authentication authentication, HttpSession session) {
        User user = currentUser(authentication);
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty() || clean.length() > GroupService.MAX_NAME) {
            return flash(session, "Group name must be 1 to " + GroupService.MAX_NAME + " characters.", MessageType.red);
        }
        if (groupService.nameTaken(user, clean)) {
            return flash(session, "You already have a group called \"" + clean + "\".", MessageType.red);
        }
        groupService.create(user, clean);
        return flash(session, "Group created.", MessageType.green);
    }

    @PostMapping("/rename/{groupId}")
    public String rename(@PathVariable("groupId") String groupId, @RequestParam("name") String name,
            Authentication authentication, HttpSession session) {
        User user = currentUser(authentication);
        ContactGroup group = groupService.getOwned(groupId, user);
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty() || clean.length() > GroupService.MAX_NAME) {
            return flash(session, "Group name must be 1 to " + GroupService.MAX_NAME + " characters.", MessageType.red);
        }
        if (!clean.equalsIgnoreCase(group.getName()) && groupService.nameTaken(user, clean)) {
            return flash(session, "You already have a group called \"" + clean + "\".", MessageType.red);
        }
        groupService.rename(group, clean);
        return flash(session, "Group renamed.", MessageType.green);
    }

    @PostMapping("/delete/{groupId}")
    public String delete(@PathVariable("groupId") String groupId, Authentication authentication,
            HttpSession session) {
        ContactGroup group = groupService.getOwned(groupId, currentUser(authentication));
        groupService.delete(group);
        return flash(session, "Group deleted. Its contacts were kept.", MessageType.green);
    }
}
