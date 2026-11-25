package com.scm.Services.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scm.Services.GroupService;
import com.scm.Services.services;
import com.scm.entity.User;
import com.scm.helper.ResourcesNotFound;
import com.scm.helper.appconstant;
import com.scm.repositories.userRepo;



@Service
public class UserserviceImpl implements services{

    @Autowired
    private GroupService groupService;

   @Autowired
   private userRepo databaseconnectivity;

   @Autowired
   private PasswordEncoder passwordEncoder;
   
   private Logger logger=LoggerFactory.getLogger(this.getClass());
    @Override
    public User saveUser(User user) {
        String userId=UUID.randomUUID().toString();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info(user.getProvider().toString());
        user.setRoleList(List.of(appconstant.Role_User));
        user.setUserid(userId);
        return databaseconnectivity.save(user);
    }

    @Override
    public Optional<User> getUserById(String id) {
       return databaseconnectivity.findById(id);
    }

    @Override
    public Optional<User> updaUser(User user) {
        User user1=databaseconnectivity.findById(user.getUserid()).orElseThrow(()-> new ResourcesNotFound("user not found"));
        user1.setName(user.getName());
        user1.setEmail(user.getEmail());
        user1.setPhoneno(user.getPhoneno());
        user1.setPassword(user.getPassword());
        user1.setAbout(user.getAbout());
        user1.setProfilepic(user.getProfilepic());
        user1.setEmailverified(user.getEmailverified());
        user1.setEnabled(user.isEnabled());
        user1.setProvider(user.getProvider());
        user1.setPhonrverified(user.isPhonrverified());
        user1.setProvidersuserid(user.getProvidersuserid());
        User save = databaseconnectivity.save(user1);
        return Optional.ofNullable(save);

    }  
    @Override
    @Transactional
    public void deleteUser(String id) {
        User user1=databaseconnectivity.findById(id).orElseThrow(()-> new ResourcesNotFound("user not found"));
        groupService.deleteAllForUser(user1);   // groups reference the user, so remove them first
        User fresh=databaseconnectivity.findById(id).orElseThrow(()-> new ResourcesNotFound("user not found"));
        databaseconnectivity.delete(fresh);
    }

    @Override
    public boolean isUserExist(String userId) {
        User user1=databaseconnectivity.findById(userId).orElse(null);
        return user1 !=null ? true : false;

    }

    @Override
    public boolean isUserExistByEmail(String email) {
       User user = databaseconnectivity.findByEmail(email).orElse(null);
       return user !=null ? true : false;
    }

    @Override
    public List<User> getAllUsers() {
       return databaseconnectivity.findAll();
    }

    @Override
    public User getUserByEmailsUser(String username) {
       return databaseconnectivity.findByEmail(username).orElseThrow(() -> new ResourcesNotFound("user not found"));
    }

    @Override
    public User updateProfile(String userid, String name, String phoneno, String about, String pictureUrl, String picturePublicId) {
        User user = databaseconnectivity.findById(userid).orElseThrow(() -> new ResourcesNotFound("user not found"));
        user.setName(name);
        user.setPhoneno(phoneno);
        user.setAbout(about);
        if (pictureUrl != null) {            // null means "keep the current picture"
            user.setProfilepic(pictureUrl);
            user.setProfilepicpublicid(picturePublicId);
        }
        return databaseconnectivity.save(user);
    }

    @Override
    public boolean passwordMatches(User user, String rawPassword) {
        return rawPassword != null && user.getPassword() != null && passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Override
    public void changePassword(String userid, String newPassword) {
        User user = databaseconnectivity.findById(userid).orElseThrow(() -> new ResourcesNotFound("user not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        databaseconnectivity.save(user);
    }

}
