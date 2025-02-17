package com.springboot.board.controller;

import com.springboot.auth.utils.MemberDetailsService;
import com.springboot.board.dto.BoardtDto;
import com.springboot.board.entity.Board;
import com.springboot.board.mapper.BoardMapper;
import com.springboot.board.service.BoardService;
import com.springboot.dto.MultiResponseDto;
import com.springboot.dto.SingleResponseDto;
import com.springboot.dto.messageResponseDto;
import com.springboot.like.service.LikeService;
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
    private final LikeService likeService;

    public BoardController(BoardService boardService, BoardMapper mapper, LikeService likeService) {
        this.boardService = boardService;
        this.mapper = mapper;
        this.likeService = likeService;
    }

    @PostMapping
    //MemberDetailsService에서 사용자 정보를 추출한다.
    //AuthenticationPrincipla 애너테이션은 Authentication에 담긴 Principle를 가져온다. (중요!)
    //즉, SecurityContext에서 MemberDetails 객체를 가져오는건데 Member를 상속받고 있어 Member 타입도 가능!
    public ResponseEntity postBoard(@Valid @RequestBody BoardtDto.Post boardPostDto,
                                    @AuthenticationPrincipal Member member) {
        Board board = boardService.createBoard(mapper.boardPostDtoToBoard(boardPostDto), member.getMemberId());
        //String message = "게시판이 등록되었습니다.";

        return new ResponseEntity<>(new SingleResponseDto<>(mapper.boardToBoardResponseDto(board)), HttpStatus.CREATED);
    }

    @PatchMapping("/{board-id}")
    public ResponseEntity patchBoard(@PathVariable("board-id") @Positive long boardId,
                                      @Valid @RequestBody BoardtDto.Patch boardPatchDto,
                                      @AuthenticationPrincipal Member member){
        boardPatchDto.setBoardId(boardId);
        Board board = boardService.updateBoard(mapper.boardPatchDtoToBoard(boardPatchDto), member.getMemberId());

        return new ResponseEntity<>(new SingleResponseDto<>(mapper.boardToBoardResponseDto(board)), HttpStatus.OK);
    }

    @GetMapping("/{board-id}")
    public ResponseEntity getBoard(@PathVariable("board-id") @Positive long boardId,
                                   @AuthenticationPrincipal Member member){
        Board board = boardService.findBoard(boardId, member.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.boardToBoardResponseDto(board)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getBoards(@Positive @RequestParam int page,
                                     @Positive @RequestParam int size,
                                    @RequestParam String sort,
                                    @AuthenticationPrincipal Member member){
        Page<Board> boardPage = boardService.findBoards(page -1, size, sort, member.getMemberId());
        List<Board> boards = boardPage.getContent();

        return new ResponseEntity<>(new MultiResponseDto<>
                (mapper.boardsToBoardResponsesDtos(boards),boardPage),HttpStatus.OK);
    }

    @DeleteMapping("/{board-id}")
    public ResponseEntity deleteBoard(@PathVariable("board-id") @Positive long boardId,
                                       @AuthenticationPrincipal Member member){
        boardService.deleteBoard(boardId, member.getMemberId());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //liks, comment, view는 모두 board의 종속된다. (애그리거트 루트를 통해야한다.)
    //그러므로 board 컨트롤러 계층에 만드는것이 더 좋은 코드
    @PostMapping("/{board-id}/like")
    public ResponseEntity postLike(@PathVariable("board-id") @Positive long boardId,
                                   @AuthenticationPrincipal Member member){
        likeService.addLike(boardId, member.getMemberId());
        String message = "좋아요가 추가되었습니다.";
        return new ResponseEntity<>(new messageResponseDto(message), HttpStatus.CREATED);
    }

    @DeleteMapping("/{board-id}/like")
    public ResponseEntity deleteLike(@PathVariable("board-id") @Positive long boardId,
                                     @AuthenticationPrincipal Member member){
        likeService.deleteLike(boardId, member.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
