package com.scm.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.Services.contactservice;
import com.scm.Services.imageservice;
import com.scm.Services.services;
import com.scm.Userform.contactform;
import com.scm.Userform.contactsearchform;
import com.scm.entity.Contact;
import com.scm.entity.ContactGroup;
import com.scm.entity.User;
import com.scm.Services.GroupService;
import com.scm.helper.CsvHelper;
import com.scm.helper.TagHelper;
import com.scm.helper.Helper;
import com.scm.helper.Message;
import com.scm.helper.MessageType;
import com.scm.helper.ResourcesNotFound;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user/contacts")
public class contactcontroller {

    Logger logger = LoggerFactory.getLogger(contactcontroller.class);

    private static final long MAX_IMPORT_BYTES = 1024 * 1024; // 1 MB
    private static final int MAX_IMPORT_ROWS = 500;
    private static final Pattern PHONE = Pattern.compile("^[0-9]{10}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Autowired
    private imageservice imageservice;

    @Autowired
    private contactservice contactservice;

    @Autowired
    private services uServices;

    @Autowired
    private GroupService groupService;

    /** Groups for the add/edit dropdowns. */
    private void addGroupList(Model model, User user) {
        model.addAttribute("groups", groupService.list(user));
    }

    /** Groups and tags shown as filter chips above the contact list. */
    private void addListExtras(Model model, User user) {
        model.addAttribute("groups", groupService.list(user));
        model.addAttribute("allTags", contactservice.getDistinctTags(user));
    }

    /** Resolves the chosen group id; null when none chosen. Throws 404 if it is not the user's group. */
    private ContactGroup resolveGroup(String groupId, User user) {
        if (groupId == null || groupId.isBlank()) {
            return null;
        }
        return groupService.getOwned(groupId, user);
    }

    /** Loads a contact only if it belongs to the logged-in user, otherwise 404. */
    private Contact ownedContact(String id, Authentication authentication) {
        User user = uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
        Contact contact = contactservice.getbyid(id);
        if (contact.getUser() == null || !contact.getUser().getUserid().equals(user.getUserid())) {
            throw new ResourcesNotFound("Contact not found");
        }
        return contact;
    }

    @RequestMapping("/add")
    public String addcontact(Model model, Authentication authentication) {
        addGroupList(model, uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication)));
        contactform fContactform = new contactform();
        model.addAttribute("contactform", fContactform);
        return "user/addcontact";
    }

    @RequestMapping(value = "/adds", method = RequestMethod.POST)
    public String savecontact(@Valid @ModelAttribute contactform ContactForm, BindingResult result, HttpSession session,
            Authentication authentication, Model model) {

        String username = Helper.getUserByEmail(authentication);
        User user = uServices.getUserByEmailsUser(username);

        // Duplicate detection (same email or same phone already in this user's contacts)
        if (!result.hasErrors()) {
            if (ContactForm.getEmail() != null && !ContactForm.getEmail().isBlank()
                    && contactservice.isDuplicate(user, ContactForm.getEmail(), null)) {
                result.rejectValue("email", "duplicate", "You already have a contact with this email");
            }
            if (contactservice.isDuplicate(user, null, ContactForm.getPhoneno())) {
                result.rejectValue("phoneno", "duplicate", "You already have a contact with this phone number");
            }
        }

        ContactGroup chosenGroup = result.hasErrors() ? null : resolveGroup(ContactForm.getGroupId(), user);

        if (result.hasErrors()) {
            addGroupList(model, user);
            return "user/addcontact";
        }

        // Picture is optional
        String filename = null;
        String fileurl = null;
        MultipartFile picture = ContactForm.getPicture();
        if (picture != null && !picture.isEmpty()) {
            filename = UUID.randomUUID().toString();
            fileurl = imageservice.uploadimage(picture, filename);
        }

        Contact contact = new Contact();
        contact.setName(ContactForm.getName());
        contact.setEmail(ContactForm.getEmail());
        contact.setAddress(ContactForm.getAddress());
        contact.setPhoneno(ContactForm.getPhoneno());
        contact.setDiscreption(ContactForm.getDiscreption());
        contact.setFavourite(ContactForm.isFavourite());
        contact.setPicture(fileurl);
        contact.setUser(user);
        contact.setContactimagepublicid(filename);
        contact.setContactGroup(chosenGroup);
        contact.setTags(TagHelper.parse(ContactForm.getTags()));
        contactservice.save(contact);

        Message message = Message.builder().content("Contact Added Successfully").messageType(MessageType.green)
                .build();
        session.setAttribute("message", message);
        return "redirect:/user/contacts/add";
    }

    @RequestMapping
    public String viewcontact(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pagesize", defaultValue = "10") int pagesize,
            @RequestParam(value = "sortby", defaultValue = "name") String sortby,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model, Authentication authentication) {

        String username = Helper.getUserByEmail(authentication);
        User userByEmailsUser = uServices.getUserByEmailsUser(username);
        Page<Contact> pageContact = contactservice.getByuser(userByEmailsUser, page, pagesize, sortby, direction);
        model.addAttribute("pageContact", pageContact);
        model.addAttribute("pageTitle", "Manage Your Contacts");
        model.addAttribute("basePath", "/user/contacts");
        model.addAttribute("favouritesView", false);
        model.addAttribute("contactsearchform", new contactsearchform());
        addListExtras(model, userByEmailsUser);
        return "user/contacts";
    }

    @RequestMapping("/search")
    public String Search(
            @ModelAttribute contactsearchform contactsearchform,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pagesize", defaultValue = "10") int size,
            @RequestParam(value = "sortby", defaultValue = "name") String sortby,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model, Authentication authentication) {

        logger.info("field {} keyword {}", contactsearchform.getField(), contactsearchform.getKeyword());

        User user = uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
        String field = contactsearchform.getField() == null ? "name" : contactsearchform.getField();
        String keyword = contactsearchform.getKeyword() == null ? "" : contactsearchform.getKeyword().trim();
        contactsearchform.setField(field);
        contactsearchform.setKeyword(keyword);

        Page<Contact> pageContact;
        if (field.equals("email")) {
            pageContact = contactservice.searchByEmail(keyword, page, size, sortby, direction, user);
        } else if (field.equals("phone")) {
            pageContact = contactservice.searchByPhoneno(keyword, page, size, sortby, direction, user);
        } else {
            pageContact = contactservice.searchByName(keyword, page, size, sortby, direction, user);
        }

        model.addAttribute("contactsearchform", contactsearchform);
        model.addAttribute("pageContact", pageContact);

        return "user/search";
    }

    @RequestMapping("/delete/{contactid}")
    public String delete(@PathVariable("contactid") String id, Authentication authentication) {
        Contact owned = ownedContact(id, authentication);
        imageservice.deleteimage(owned.getContactimagepublicid());
        contactservice.delete(id);
        logger.info("contactid {} deleted", id);
        return "redirect:/user/contacts";
    }

    @RequestMapping("/view/{contactid}")
    public String view(@PathVariable("contactid") String id,
            Model model, Authentication authentication) {
        var contact = ownedContact(id, authentication);
        contactform contacts = new contactform();
        contacts.setName(contact.getName());
        contacts.setAddress(contact.getAddress());
        contacts.setEmail(contact.getEmail());
        contacts.setPhoneno(contact.getPhoneno());
        contacts.setDiscreption(contact.getDiscreption());
        contacts.setFavourite(contact.isFavourite());
        contacts.setContactimg(contact.getPicture());
        contacts.setGroupId(contact.getContactGroup() == null ? null : contact.getContactGroup().getId());
        contacts.setTags(TagHelper.join(contact.getTags(), ", "));
        addGroupList(model, uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication)));
        model.addAttribute("contactform", contacts);
        model.addAttribute("contactid", id);
        return "user/update_contact_view";
    }

    @RequestMapping(value = "/update/{contactid}",method = RequestMethod.POST)
    public String update(@PathVariable("contactid") String id,
           @Valid @ModelAttribute contactform update,BindingResult bindingResult,
            Model model, HttpSession session, Authentication authentication) {

                Contact contact = ownedContact(id, authentication);

                User currentUser = uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
                if(bindingResult.hasErrors()){
                    model.addAttribute("contactid", id);
                    addGroupList(model, currentUser);
                    return "user/update_contact_view";
                }
                

                
        contact.setId(id);
        contact.setName(update.getName());
        contact.setEmail(update.getEmail());
        contact.setAddress(update.getAddress());
        contact.setDiscreption(update.getDiscreption());
        contact.setFavourite(update.isFavourite());
        contact.setPhoneno(update.getPhoneno());
        contact.setContactGroup(resolveGroup(update.getGroupId(), currentUser));
        contact.setTags(TagHelper.parse(update.getTags()));
        // contact.setPicture(update.getContactimg());
        // contactform contactss = new contactform();
        if (update.getPicture() != null && !update.getPicture().isEmpty()) {
            String oldImageId = contact.getContactimagepublicid();
            String filename = UUID.randomUUID().toString();
            String fileurl = imageservice.uploadimage(update.getPicture(), filename);
            imageservice.deleteimage(oldImageId);
            contact.setContactimagepublicid(filename);
            contact.setPicture(fileurl);
            update.setContactimg(fileurl);
        }
        logger.info(update.getContactimg());
        contactservice.update(contact);
        session.setAttribute("message",
                Message.builder().content("Updated Successfully!!").messageType(MessageType.green).build());
        return "redirect:/user/contacts/view/"+id;
    }

    // ---------------------------------------------------------------- groups & tags

    @GetMapping("/group/{groupId}")
    public String byGroup(@PathVariable("groupId") String groupId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pagesize", defaultValue = "10") int pagesize,
            @RequestParam(value = "sortby", defaultValue = "name") String sortby,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model, Authentication authentication) {

        User user = uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
        ContactGroup group = groupService.getOwned(groupId, user);
        model.addAttribute("pageContact", contactservice.getByGroup(user, group, page, pagesize, sortby, direction));
        model.addAttribute("pageTitle", "Group: " + group.getName());
        model.addAttribute("basePath", "/user/contacts/group/" + group.getId());
        model.addAttribute("favouritesView", false);
        model.addAttribute("contactsearchform", new contactsearchform());
        addListExtras(model, user);
        return "user/contacts";
    }

    @GetMapping("/tag/{tag}")
    public String byTag(@PathVariable("tag") String rawTag,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pagesize", defaultValue = "10") int pagesize,
            @RequestParam(value = "sortby", defaultValue = "name") String sortby,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model, Authentication authentication) {

        String tag = TagHelper.normalize(rawTag);
        if (tag.isEmpty()) {
            return "redirect:/user/contacts";
        }
        User user = uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
        model.addAttribute("pageContact", contactservice.getByTag(user, tag, page, pagesize, sortby, direction));
        model.addAttribute("pageTitle", "Tag: #" + tag);
        model.addAttribute("basePath", "/user/contacts/tag/" + tag);
        model.addAttribute("favouritesView", false);
        model.addAttribute("contactsearchform", new contactsearchform());
        addListExtras(model, user);
        return "user/contacts";
    }

    // ---------------------------------------------------------------- favourites

    @GetMapping("/favourites")
    public String favourites(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pagesize", defaultValue = "10") int pagesize,
            @RequestParam(value = "sortby", defaultValue = "name") String sortby,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model, Authentication authentication) {

        User user = uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
        Page<Contact> pageContact = contactservice.getFavourites(user, page, pagesize, sortby, direction);
        model.addAttribute("pageContact", pageContact);
        model.addAttribute("pageTitle", "Favourite Contacts");
        model.addAttribute("basePath", "/user/contacts/favourites");
        model.addAttribute("favouritesView", true);
        model.addAttribute("contactsearchform", new contactsearchform());
        addListExtras(model, user);
        return "user/contacts";
    }

    @PostMapping("/favourite/{contactid}")
    public String toggleFavourite(@PathVariable("contactid") String id,
            @RequestParam(value = "from", defaultValue = "contacts") String from,
            Authentication authentication) {
        ownedContact(id, authentication);
        contactservice.toggleFavourite(id);
        return "favourites".equals(from) ? "redirect:/user/contacts/favourites" : "redirect:/user/contacts";
    }

    // ---------------------------------------------------------------- export

    @GetMapping("/export")
    public void exportContacts(HttpServletResponse response, Authentication authentication) throws IOException {
        User user = uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
        List<Contact> contacts = contactservice.getAllForUser(user);

        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"contacts.csv\"");

        PrintWriter out = response.getWriter();
        out.write('\uFEFF'); // BOM so Excel reads UTF-8 correctly
        out.write("name,email,phone,address,description,favourite,group,tags\r\n");
        for (Contact c : contacts) {
            out.write(CsvHelper.escape(c.getName()) + ","
                    + CsvHelper.escape(c.getEmail()) + ","
                    + CsvHelper.escape(c.getPhoneno()) + ","
                    + CsvHelper.escape(c.getAddress()) + ","
                    + CsvHelper.escape(c.getDiscreption()) + ","
                    + c.isFavourite() + ","
                    + CsvHelper.escape(c.getContactGroup() == null ? "" : c.getContactGroup().getName()) + ","
                    + CsvHelper.escape(TagHelper.join(c.getTags(), ";")) + "\r\n");
        }
        out.flush();
    }

    // ---------------------------------------------------------------- import

    @GetMapping("/import")
    public String importPage() {
        return "user/import";
    }

    @PostMapping("/import")
    public String importContacts(@RequestParam("file") MultipartFile file, HttpSession session,
            Authentication authentication) {

        if (file == null || file.isEmpty() || file.getSize() > MAX_IMPORT_BYTES) {
            return flash(session, "Please choose a CSV file smaller than 1 MB.", MessageType.red);
        }

        List<List<String>> rows;
        try {
            rows = CsvHelper.parse(new String(file.getBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return flash(session, "Could not read the file.", MessageType.red);
        }
        if (rows.size() < 2) {
            return flash(session, "The file has no contact rows.", MessageType.red);
        }

        // Map header names to column positions
        Map<String, Integer> col = new HashMap<>();
        List<String> header = rows.get(0);
        for (int i = 0; i < header.size(); i++) {
            col.put(header.get(i).trim().toLowerCase(), i);
        }
        Integer phoneCol = col.containsKey("phone") ? col.get("phone") : col.get("phoneno");
        if (!col.containsKey("name") || !col.containsKey("email") || phoneCol == null) {
            return flash(session, "CSV must have the columns: name, email, phone.", MessageType.red);
        }

        User user = uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
        int imported = 0, duplicates = 0, invalid = 0, ignored = 0;
        Map<String, ContactGroup> groupCache = new HashMap<>();

        for (int i = 1; i < rows.size(); i++) {
            if (i > MAX_IMPORT_ROWS) {
                ignored = rows.size() - i;
                break;
            }
            List<String> r = rows.get(i);
            String name = cell(r, col.get("name"));
            String email = cell(r, col.get("email"));
            String phone = cell(r, phoneCol);

            if (name.isBlank() || !EMAIL.matcher(email).matches() || !PHONE.matcher(phone).matches()) {
                invalid++;
                continue;
            }
            if (contactservice.isDuplicate(user, email, phone)) {
                duplicates++;
                continue;
            }

            Contact contact = new Contact();
            contact.setName(name);
            contact.setEmail(email);
            contact.setPhoneno(phone);
            contact.setAddress(cell(r, col.get("address")));
            contact.setDiscreption(cell(r, col.containsKey("description") ? col.get("description") : col.get("discreption")));
            String fav = cell(r, col.get("favourite")).toLowerCase();
            contact.setFavourite(fav.equals("true") || fav.equals("yes") || fav.equals("1"));
            contact.setUser(user);
            String groupName = cell(r, col.get("group"));
            if (!groupName.isBlank()) {
                String shortName = groupName.length() > GroupService.MAX_NAME ? groupName.substring(0, GroupService.MAX_NAME) : groupName;
                contact.setContactGroup(groupCache.computeIfAbsent(shortName.toLowerCase(), k -> groupService.findOrCreate(user, shortName)));
            }
            contact.setTags(TagHelper.parse(cell(r, col.get("tags"))));
            contactservice.save(contact);
            imported++;
        }

        String summary = "Imported " + imported + " contact(s), skipped " + duplicates + " duplicate(s), "
                + invalid + " invalid row(s)" + (ignored > 0 ? ", ignored " + ignored + " rows over the " + MAX_IMPORT_ROWS + " row limit" : "") + ".";
        session.setAttribute("message", Message.builder().content(summary)
                .messageType(imported > 0 ? MessageType.green : MessageType.yellow).build());
        return "redirect:/user/contacts";
    }

    private static String cell(List<String> row, Integer index) {
        if (index == null || index >= row.size() || row.get(index) == null) {
            return "";
        }
        return row.get(index).trim();
    }

    private String flash(HttpSession session, String text, MessageType type) {
        session.setAttribute("message", Message.builder().content(text).messageType(type).build());
        return "redirect:/user/contacts/import";
    }
}
