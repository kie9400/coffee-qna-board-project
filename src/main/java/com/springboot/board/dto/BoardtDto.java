package com.springboot.board.dto;

import com.springboot.board.entity.Board;
import com.springboot.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class BoardtDto {
    @Getter
    public static class Post {
        @NotBlank(message = "제목은 공백이 아니어야 합니다.")
        private String title;

        @NotBlank(message = "내용은 최소한 1글자라도 있어야 합니다.")
        private String content;

        @NotNull
        private Board.VisibilityStatus visibility;
    }

    @Getter
    public static class Patch{
        @Setter
        private long BoardId;

        @NotBlank(message = "제목은 공백이 아니어야 합니다.")
        private String title;

        @NotBlank(message = "내용은 최소한 1글자라도 있어야 합니다.")
        private String content;

        @NotNull
        private Board.VisibilityStatus visibility;
    }

    @AllArgsConstructor
    @Getter
    public static class Response {
        private long boardId;
        private String title;
        private String content;
        private Board.VisibilityStatus visibilityStatus;
        private Board.BoardStauts boardStauts;

        public String getBoardStatus() {
            return boardStauts.getStatus();
        }

        public int getVisibilityStatusNumber(){
            return visibilityStatus.getStatusNumber();
        }
    }
}
