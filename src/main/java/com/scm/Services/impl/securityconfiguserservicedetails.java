package com.scm.Services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.scm.repositories.userRepo;

@Service
public class securityconfiguserservicedetails implements UserDetailsService{


    @Autowired
    private userRepo databaseconnectiRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return databaseconnectiRepo.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("user not found") );
    }

}
