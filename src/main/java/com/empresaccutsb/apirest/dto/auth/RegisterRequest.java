package com.empresaccutsb.apirest.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 4, max = 80) String username,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank
                @Size(min = 8, max = 120)
                @Pattern(
                        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
                        message =
                                "La contrasena debe incluir mayuscula, minuscula y numero")
                String password) {}
