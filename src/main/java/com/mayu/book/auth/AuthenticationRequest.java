package com.mayu.book.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationRequest {

    @Email(message = "Email is not valid")
    @NotEmpty(message = "Email is compulsory")
    @NotBlank(message = "Email is compulsory")
    private String email;

    @NotEmpty(message = "Password is compulsory")
    @NotBlank(message = "Password is compulsory")
    @Size(min=8, message = "Password need to have minimum 8 characters")
    private String password;
}
