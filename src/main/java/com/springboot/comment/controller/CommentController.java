package com.springboot.comment.controller;

import com.springboot.board.dto.BoardtDto;
import com.springboot.comment.dto.CommentDto;
import com.springboot.comment.entity.Comment;
import com.springboot.comment.mapper.CommentMapper;
import com.springboot.comment.repository.CommentRepository;
import com.springboot.comment.service.CommentService;
import com.springboot.member.entity.Member;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/api/boards/{board-id}/comments")
@Validated
public class CommentController {
    private final CommentService commentService;
    private final CommentMapper mapper;

    public CommentController(CommentService commentService, CommentMapper mapper) {
        this.commentService = commentService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity postComment(@PathVariable("board-id") @Positive long boardId,
                                      @Valid @RequestBody CommentDto.Post commentPostDto,
                                      @AuthenticationPrincipal Member member){
        Comment comment = commentService.createComment(boardId, mapper.commentPostDtoToComment(commentPostDto), member.getMemberId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //게시글 1개당 답변이 1개이므로 그냥 board-id에서 가져와도 될거같다.
    //@PatchMapping("/{comment-id}")
    @PatchMapping
    public ResponseEntity postComment(@PathVariable("board-id") @Positive long boardId,
                                      @Valid @RequestBody CommentDto.Patch commentPatchDto,
                                      @AuthenticationPrincipal Member member){
        Comment comment = commentService.updateComment(boardId, mapper.commentPatchDtoToComment(commentPatchDto), member.getMemberId());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity deleteAnswer(@PathVariable("board-id") @Positive long boardId,
                                       @AuthenticationPrincipal Member member) {
        commentService.deleteComment(boardId, member.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
