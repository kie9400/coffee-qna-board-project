package com.springboot.board.controller;

import com.springboot.auth.utils.MemberDetailsService;
import com.springboot.board.dto.BoardtDto;
import com.springboot.board.entity.Board;
import com.springboot.board.mapper.BoardMapper;
import com.springboot.board.service.BoardService;
import com.springboot.dto.SingleResponseDto;
import com.springboot.member.entity.Member;
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
    //AuthenticationPrincipla 애너테이션은 Authentication에 담긴 Principle를 가져온다. (중요!)
    //즉, SecurityContext에서 MemberDetails 객체를 가져오는건데 Member를 상속받고 있어 Member 타입도 가능!
    public ResponseEntity postOrder(@Valid @RequestBody BoardtDto.Post boardPostDto,
                                    @AuthenticationPrincipal Member member) {
        Board board = boardService.createBoard(mapper.boardPostToBoard(boardPostDto), member.getMemberId());

        return new ResponseEntity<>(new SingleResponseDto<>(mapper.boardToBoardResponse(board)), HttpStatus.CREATED);
    }
}
