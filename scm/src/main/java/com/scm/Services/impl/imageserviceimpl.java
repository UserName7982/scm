package com.scm.Services.impl;

import java.io.IOException;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.scm.Services.imageservice;
import com.scm.helper.appconstant;

@Service
public class imageserviceimpl implements imageservice {

    private Cloudinary cloudinary;

    public imageserviceimpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    @Override
    public String uploadimage(MultipartFile picture,String filename) {
        
        try {
            byte[] data=new byte[picture.getInputStream().available()];
            picture.getInputStream().read(data);
            cloudinary.uploader().upload(data, ObjectUtils.asMap(
                "public_id",filename
            ));
            return this.getUrlfrompublicid(filename);
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }
   
    }


    @Override
    public String getUrlfrompublicid(String publicid) {
  
        return cloudinary.url().
        transformation(new Transformation<>().
        width(appconstant.picture_Width)
        .height(appconstant.picture_height)
        .crop(appconstant.picture_crop))
        .generate(publicid);
    }

}
