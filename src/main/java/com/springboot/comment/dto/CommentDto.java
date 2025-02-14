package com.springboot.comment.dto;


import com.springboot.comment.entity.Comment;
import com.springboot.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class CommentDto {
    @Getter
    public static class Post {
        @NotBlank(message = "내용은 최소한 1글자라도 있어야 합니다.")
        private String content;
    }

    @Getter
    public static class Patch{
        @NotBlank(message = "내용은 최소한 1글자라도 있어야 합니다.")
        private String content;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Response {
        private String content;
        private LocalDateTime createdAt;
    }
}
