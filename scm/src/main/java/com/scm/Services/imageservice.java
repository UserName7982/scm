package com.scm.Services;

import org.springframework.web.multipart.MultipartFile;

public interface imageservice {

  String uploadimage(MultipartFile picture,String filename);

  String getUrlfrompublicid(String publicid);

}
