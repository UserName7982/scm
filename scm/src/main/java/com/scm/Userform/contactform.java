package com.scm.Userform;

import org.springframework.web.multipart.MultipartFile;

import com.scm.validator.validfile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class contactform {

    @NotBlank(message="Invalid username")
    private String name;
    @NotBlank(message = "email is requried")
    @Email(message = "Invalid email")
    private String email;
    @NotBlank(message = "phone No. is Required")
    @Pattern(regexp="^[0-9]{10}$", message="Invalid Phone No.")
    private String phoneno;
    @NotBlank(message="Address is required")
    private String address;
    @validfile
    private MultipartFile picture;
    private String contactimg;
    private String discreption;
    private boolean favourite=false;
}
