//package com.example.dto;
//
//import lombok.Getter;
//import lombok.Setter;
//
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.Max;
//import jakarta.validation.constraints.Min;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Size;
//
//@Getter
//@Setter
//public class UserCreateRequest {
//
//    @NotNull(message = "Имя не может быть пустым")
//    @Size(min = 2, max = 50, message = "Имя должно быть от 2 до 50 символов")
//    private String name;
//
//    @Email(message = "Некорректный email")
//    @NotBlank(message = "Email обязателен")
//    private String email;
//
//    @NotNull(message = "Возраст обязателен")
//    @Min(value = 1, message = "Возраст должен быть больше 0")
//    @Max(value = 150, message = "Возраст должен быть меньше или равен 150")
//    private Integer age;
//}
