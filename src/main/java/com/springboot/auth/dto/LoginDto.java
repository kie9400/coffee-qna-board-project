package com.springboot.auth.dto;

import lombok.Getter;

@Getter
public class LoginDto {
    //email이 username이다.
    private String username;
    private String password;
}
