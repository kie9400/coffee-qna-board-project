package com.springboot.like.Controller;

import com.springboot.comment.dto.CommentDto;
import com.springboot.comment.entity.Comment;
import com.springboot.member.entity.Member;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/api/boards/{board-id}/like")
@Validated
public class LikeController {


    @PostMapping
    public ResponseEntity postComment(@PathVariable("board-id") @Positive long boardId,
                                      @AuthenticationPrincipal Member member){

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
