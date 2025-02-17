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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
                                   @AuthenticationPrincipal Member member,
                                   HttpServletRequest request, HttpServletResponse response){
        Board board = boardService.findBoard(boardId, member.getMemberId());
        //조회수 증가 메서드(조회수 중복 방지도 포함한다.)
        preventDuplicateView(boardId, member.getMemberId(), request, response);
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

    //조회수 중복방지를 위해 Cookie 사용
    //쿠키는 보안이 약하다는 단점이 존재하지만 DB같은 서버에 부하를 안준다는 장점이있음
    private void preventDuplicateView(Long boardId, long memberId, HttpServletRequest request, HttpServletResponse response){
        Cookie oldCookie = getCookie(request, "boardView");

        if (oldCookie != null) {
            // 기존 쿠키 값에 memberId와 boardId 조합 확인
            String cookieValue = oldCookie.getValue();
            // 쿠키값에 [memberId-boardId] 형식으로 조회 기록이 저장된다.
            // ex)[1-2] 1번회원이 2번게시글 조회한 기록
            String targetValue = "[" + memberId + "-" + boardId + "]";

            //만약 쿠기가 있다면 해당 쿠키에 [memberId-boardId]가 포함되어있는지 확인한다.
            if (!cookieValue.contains(targetValue)) {
                //쿠키에 해당하는 회원정보가 없다면(중복 조회x) 조회수 증가
                boardService.viewCountUp(boardId);
                //쿠키 값에 사용자의 해당 게시판 조회 기록을 저장한다.
                oldCookie.setValue(cookieValue + targetValue);
                //쿠키의 경로 설정, 애플리케이션의 "/" 모든 경로에서 유효한 것
                oldCookie.setPath("/");
                //쿠키의 유효기간을 설정함으로써 10시간이 지나면 다시 조회가 가능하다.
                oldCookie.setMaxAge(60 * 60 * 10);
                //변경된 쿠키를 HTTP 응답에 추가한다.
                response.addCookie(oldCookie);
            }
        } else {
            // 첫 조회(쿠키가 null)일 경우 조회수 증가
            boardService.viewCountUp(boardId);
            // 새로운 쿠키를 생성하여 현재 사용자의 조회 기록을 저장한다.
            Cookie newCookie = new Cookie("boardView", "[" + memberId + "-" + boardId + "]");
            newCookie.setPath("/");
            newCookie.setMaxAge(60 * 60 * 10);  // 쿠키 유효기간 1일
            response.addCookie(newCookie);
        }
    }

    //쿠키를 찾기위한 메서드
    //모든 쿠키에서 boardView 쿠키를 찾아 반환하는 메서드이다.
    private Cookie getCookie(HttpServletRequest request, String cookieName){
        //HTTP 요청에서 모든 쿠키 배열을 가져온다.
        Cookie[] cookies = request.getCookies();
        //쿠키를 찾으면 해당 쿠키를 반환한다.
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }
        // 쿠키를 찾지 못하면 null을 반환한다.
        return null;
    }
}
