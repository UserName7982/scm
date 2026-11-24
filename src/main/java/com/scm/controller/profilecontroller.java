package com.scm.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.scm.Services.imageservice;
import com.scm.Services.services;
import com.scm.Userform.passwordform;
import com.scm.Userform.profileform;
import com.scm.entity.Providers;
import com.scm.entity.User;
import com.scm.helper.Helper;
import com.scm.helper.Message;
import com.scm.helper.MessageType;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user/profile")
public class profilecontroller {

    @Autowired
    private services uServices;

    @Autowired
    private imageservice imageservice;

    private User currentUser(Authentication authentication) {
        return uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
    }

    private void flash(HttpSession session, String text, MessageType type) {
        session.setAttribute("message", Message.builder().content(text).messageType(type).build());
    }

    // ------------------------------------------------------------ edit profile

    @GetMapping("/edit")
    public String editPage(Model model, Authentication authentication) {
        User user = currentUser(authentication);
        profileform form = new profileform();
        form.setName(user.getName());
        form.setPhoneno(user.getPhoneno());
        form.setAbout(user.getAbout());
        model.addAttribute("profileform", form);
        return "user/edit_profile";
    }

    @PostMapping("/edit")
    public String editSave(@Valid @ModelAttribute("profileform") profileform form, BindingResult result,
            Authentication authentication, HttpSession session) {

        User user = currentUser(authentication);

        String pictureUrl = null;
        String picturePublicId = null;
        MultipartFile picture = form.getPicture();

        if (!result.hasErrors() && picture != null && !picture.isEmpty()) {
            picturePublicId = UUID.randomUUID().toString();
            pictureUrl = imageservice.uploadimage(picture, picturePublicId);
            if (pictureUrl == null) {
                result.rejectValue("picture", "upload", "Image upload failed, please try again");
            }
        }

        if (result.hasErrors()) {
            return "user/edit_profile";
        }

        String phone = form.getPhoneno() == null || form.getPhoneno().isBlank() ? null : form.getPhoneno().trim();
        String about = form.getAbout() == null || form.getAbout().isBlank() ? null : form.getAbout().trim();
        String oldPublicId = user.getProfilepicpublicid();

        uServices.updateProfile(user.getUserid(), form.getName().trim(), phone, about, pictureUrl, picturePublicId);

        if (pictureUrl != null) {
            imageservice.deleteimage(oldPublicId); // remove the replaced picture from Cloudinary
        }

        flash(session, "Profile updated successfully", MessageType.green);
        return "redirect:/user/profile";
    }

    // ------------------------------------------------------------ change password

    @GetMapping("/password")
    public String passwordPage(Model model) {
        model.addAttribute("passwordform", new passwordform());
        return "user/change_password";
    }

    @PostMapping("/password")
    public String passwordSave(@Valid @ModelAttribute("passwordform") passwordform form, BindingResult result,
            Authentication authentication, HttpSession session) {

        User user = currentUser(authentication);

        // Google-login accounts have no password of their own
        if (user.getProvider() != Providers.SELF) {
            flash(session, "Your account signs in with Google, so there is no password to change.", MessageType.yellow);
            return "redirect:/user/profile";
        }

        if (!result.hasErrors()) {
            if (!uServices.passwordMatches(user, form.getCurrentPassword())) {
                result.rejectValue("currentPassword", "wrong", "Current password is incorrect");
            } else if (!form.getNewPassword().equals(form.getConfirmPassword())) {
                result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
            } else if (form.getNewPassword().equals(form.getCurrentPassword())) {
                result.rejectValue("newPassword", "same", "New password must be different from the current one");
            }
        }

        if (result.hasErrors()) {
            return "user/change_password";
        }

        uServices.changePassword(user.getUserid(), form.getNewPassword());
        flash(session, "Password changed successfully", MessageType.green);
        return "redirect:/user/profile";
    }
}
