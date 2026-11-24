package com.scm.Userform;

import org.springframework.web.multipart.MultipartFile;

import com.scm.validator.validfile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class profileform {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be 3 to 100 characters")
    private String name;

    // optional; when given it must be 8 to 12 digits (same range registration allows)
    @Pattern(regexp = "^$|^[0-9]{8,12}$", message = "Phone must be 8 to 12 digits")
    private String phoneno;

    @Size(max = 1000, message = "About can be at most 1000 characters")
    private String about;

    @validfile
    private MultipartFile picture;
}
