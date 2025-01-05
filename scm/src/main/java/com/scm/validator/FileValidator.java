package com.scm.validator;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FileValidator implements ConstraintValidator<validfile,MultipartFile>{


    public static final long MAX_FILE_SIAZE=1024*1024*2;//2MB

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        
        if(file==null || file.isEmpty() ){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File Cannot Be Empty").addConstraintViolation();
            return true;
        }
        if(file.getSize()>MAX_FILE_SIAZE){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File Size Should BE Less Than 2MB").addConstraintViolation();
            return false;
        }

        return true;
        
    }

}
