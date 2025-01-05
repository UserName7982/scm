package com.scm.Services;

import java.util.Optional;
import java.util.*;
import com.scm.entity.User;

public interface services {

    User saveUser(User user);
    Optional <User> getUserById(String id);
    Optional <User> updaUser(User user);
    void deleteUser(String id);
    boolean isUserExist(String userId);
    boolean isUserExistByEmail(String email);
    List <User> getAllUsers();
    User getUserByEmailsUser(String username);
}
