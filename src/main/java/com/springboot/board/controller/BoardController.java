package com.springboot.board.controller;

import com.springboot.auth.utils.MemberDetailsService;
import com.springboot.board.dto.BoardtDto;
import com.springboot.board.entity.Board;
import com.springboot.board.mapper.BoardMapper;
import com.springboot.board.service.BoardService;
import com.springboot.dto.MultiResponseDto;
import com.springboot.dto.SingleResponseDto;
import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.Member;
import org.apache.tomcat.util.http.fileupload.MultipartStream;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

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

    @PatchMapping("/{board-id}")
    public ResponseEntity patchMember(@PathVariable("board-id") @Positive long boardId,
                                      @Valid @RequestBody BoardtDto.Patch boardPatchDto,
                                      @AuthenticationPrincipal Member member){
        boardPatchDto.setBoardId(boardId);
        Board board = boardService.updateBoard(mapper.boardPatchToBoard(boardPatchDto), member.getMemberId());

        return new ResponseEntity<>(new SingleResponseDto<>(mapper.boardToBoardResponse(board)), HttpStatus.OK);
    }

    @GetMapping("/{board-id}")
    public ResponseEntity getBoard(@PathVariable("board-id") @Positive long boardId,
                                   @AuthenticationPrincipal Member member){
        Board board = boardService.findBoard(boardId, member.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.boardToBoardResponse(board)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getBoards(@Positive @RequestParam int page,
                                     @Positive @RequestParam int size,
                                    @RequestParam String sort){
        Page<Board> boardPage = boardService.findBoards(page -1, size, sort);
        List<Board> boards = boardPage.getContent();

        return new ResponseEntity<>(new MultiResponseDto<>
                (mapper.boardsToBoardResponses(boards),boardPage),HttpStatus.OK);
    }

    @DeleteMapping("/{board-id}")
    public ResponseEntity deleteMember(@PathVariable("board-id") @Positive long boardId,
                                       @AuthenticationPrincipal Member member){
        boardService.deleteBoard(boardId, member.getMemberId());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
