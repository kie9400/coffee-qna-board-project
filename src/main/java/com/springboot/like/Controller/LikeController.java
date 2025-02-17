package com.springboot.like.Controller;

import com.springboot.dto.messageResponseDto;
import com.springboot.like.service.LikeService;
import com.springboot.member.entity.Member;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/api/boards/{board-id}/like")
@Validated
public class LikeController {
    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    public ResponseEntity postLike(@PathVariable("board-id") @Positive long boardId,
                                      @AuthenticationPrincipal Member member){
        likeService.addLike(boardId, member.getMemberId());
        String message = "좋아요가 추가되었습니다.";
        return new ResponseEntity<>(new messageResponseDto(message), HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity deleteLike(@PathVariable("board-id") @Positive long boardId,
                                     @AuthenticationPrincipal Member member){
        likeService.deleteLike(boardId, member.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
