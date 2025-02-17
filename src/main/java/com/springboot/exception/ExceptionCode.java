package com.springboot.exception;

import lombok.Getter;

public enum ExceptionCode {
    MEMBER_NOT_FOUND(404,"Member not found"),
    MEMBER_EXISTS(409,"Member exists"),
    BOARD_NOT_FOUND(404,"Board not found"),
    BOARD_EXISTS(409,"Board exists"),
    COMMENT_NOT_FOUND(404,"Comment not found"),
    COMMENT_EXISTS(409,"Comment exists"),
    NOT_IMPLEMENTATION(501, "Not Implementation"),
    INVALID_MEMBER_STATUS(400, "Invalid member status"),
    UNAUTHORIZED_MEMBER_ACCESS(401,"Not authorized to access this resource"),
    FORBIDDEN_OPERATION(403, "You are not allowed to create a board"),
    LIKE_ALREADY_EXISTS(409,"You have already liked this board."),
    LIKE_NOT_CLICK(404, "You are not click like");

    @Getter
    private int status;

    @Getter
    private String message;

    ExceptionCode(int statusCode, String message){
        this.message = message;
        this.status = statusCode;
    }
}
