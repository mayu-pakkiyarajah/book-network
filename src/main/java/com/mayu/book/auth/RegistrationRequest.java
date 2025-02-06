package com.mayu.book.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class RegistrationRequest {

    @NotBlank(message = "First name is compulsory")
    private String firstName;

    @NotBlank(message = "Last name is compulsory")
    private String lastName;

    @Email(message = "Email is not valid")
    @NotBlank(message = "Email is compulsory")
    private String email;

    @NotBlank(message = "Password is compulsory")
    @Size(min=8, message = "Password need to have minimum 8 characters")
    private String password;
}
