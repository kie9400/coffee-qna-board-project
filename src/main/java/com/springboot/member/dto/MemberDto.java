package com.springboot.member.dto;

import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class MemberDto {
    @Getter
    public static class Post {
        @NotBlank(message = "이메일은 공백이 아니어야 합니다.")
        @Email
        private String email;

        @NotBlank
        private String password;

        @NotBlank(message = "닉네임은 공백이 아니어야 합니다.")
        private String nickName;
    }
}
