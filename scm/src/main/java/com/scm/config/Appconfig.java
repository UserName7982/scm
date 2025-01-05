package com.scm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;


@Configuration
public class Appconfig {

    @Value("${cloudinary.cloud.name}")
    private String cloud_name;

    @Value("${cloudinary.api.key}")
    private String api_key;

    @Value("${cloudinary.api.secret}")
    private String apisecret;


    @Bean
    public Cloudinary cloudinary(){

        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloud_name,
            "api_key",api_key,
            "api_secret",apisecret
        ));

    }

}
