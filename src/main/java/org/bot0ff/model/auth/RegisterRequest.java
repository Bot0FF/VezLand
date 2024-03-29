package org.bot0ff.model.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "The username is required.")
    //@Size(min = 3, max = 20, message = "The username must be from 3 to 20 characters.")
    private String username;
    @NotEmpty(message = "The email is required.")
    //@Email(message = "The email is not a valid email.")
    private String email;
    @NotBlank(message = "The password is required.")
    //@Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*()]).{8,}$", message = "Password must be 8 characters long and combination of uppercase letters, lowercase letters, numbers, special characters.")
    private String password;
}
