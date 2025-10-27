package com.example.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UserUpdateRequest {

    @NotNull(message = "ID обязателен для обновления")
    private Long id;

    @NotNull
    @Size(min = 2, max = 50)
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotNull
    @Min(1)
    @Max(150)
    private Integer age;
}
