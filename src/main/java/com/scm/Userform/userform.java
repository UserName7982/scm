package com.scm.Userform;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class userform {
    @NotBlank(message = "Invalid Username")
    @Size(min = 3, message = "Min 3 Char Required")
    private String name;
    @Email(message = "InValid Email")
    @NotBlank(message = "Email is required")
    private String email;
    private String about;
    @NotBlank(message = "Invalid Password")
    @Size(min = 6, message = "Min 6 char required")
    private String password;
    @Size(min = 8, max = 12, message = "minimum 8 char required")
    private String phoneno;
}
