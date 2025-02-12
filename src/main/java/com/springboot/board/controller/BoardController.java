package com.springboot.board.controller;

import com.springboot.auth.utils.MemberDetailsService;
import com.springboot.board.dto.BoardtDto;
import com.springboot.board.entity.Board;
import com.springboot.board.mapper.BoardMapper;
import com.springboot.board.service.BoardService;
import com.springboot.dto.SingleResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/boards")
@Validated
public class BoardController {
    private final BoardService boardService;
    private final BoardMapper mapper;

    public BoardController(BoardService boardService, BoardMapper mapper) {
        this.boardService = boardService;
        this.mapper = mapper;
    }

    @PostMapping
    //MemberDetailsService에서 사용자 정보를 추출한다.
    public ResponseEntity postOrder(@Valid @RequestBody BoardtDto.Post boardPostDto) {
        Board board = boardService.createBoard(mapper.boardPostToBoard(boardPostDto));

        return new ResponseEntity<>(new SingleResponseDto<>(mapper.boardToBoardResponse(board)), HttpStatus.CREATED);
    }
}
