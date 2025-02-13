package com.springboot.board.service;

import com.springboot.board.entity.Board;
import com.springboot.board.repository.BoardRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberService memberService;

    public BoardService(BoardRepository boardRepository, MemberService memberService) {
        this.boardRepository = boardRepository;
        this.memberService = memberService;
    }

    //질문글 작성
    public Board createBoard(Board board, long memberId){
        //작성자(회원)이 실제 가입되어있는 회원인지 검증
        Member member = memberService.findVerifiedMember(memberId);

        //관리자는 질문글을 등록할 수 없다.
        if(member.getRoles().contains("ADMIN")){
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN_OPERATION);
        }

        board.setMember(member);
        return boardRepository.save(board);
    }

    //질문글 수정
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public Board updateBoard(Board board, long memberId){
        //작성자(회원)이 실제 가입되어있는 회원인지 검증
        Member member = memberService.findVerifiedMember(memberId);

        //해당 게시판이 존재하는지 확인
        findVerifiedBoard(board.getBoardId());

        //회원이 게시글의 작성자가 맞는지 확인
        //수정은 작성자만 가능해야 하며 관리자는 할 수 없기에 작성자인지만 검증한다.
        isBoardOwner(board, memberId);

        Optional.ofNullable(board.getTitle())
                .ifPresent(title -> board.setTitle(title));
        Optional.ofNullable(board.getContent())
                .ifPresent(content -> board.setContent(content));
        Optional.ofNullable(board.getVisibility())
                .ifPresent(visibility -> board.setVisibility(visibility));
//      상태수정은 불가능한게 맞다. 만약 삭제된 글인데 회원이 수정가능하면 안된다.
//      Optional.ofNullable(board.getBoardStauts())
//              .ifPresent(status -> board.setBoardStauts(status));

        //답변 완료 상태로의 수정은 관리자만 가능해야한다.
        //글 삭제, 글 비활성화, 답변완료 상태에서는 수정이 되어서는 안된다.
        if(!board.getBoardStauts().equals(Board.BoardStauts.QUESTION_REGISTERED)){
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN_OPERATION);
        }

        return boardRepository.save(board);
    }

    //특정 질문글 조회
    @Transactional(readOnly = true)
    public Board findBoard(long boardId, long memberId){
        Board board = findVerifiedBoard(boardId);
        //게시판이 비밀글인지 확인하여 비밀글이면 작성자, 관리자만 조회가능 하도록한다.
        isSecretBoard(board, memberId);

        return board;
    }
    //전체 질문글 조회
    @Transactional(readOnly = true)
    public Page<Board> findBoards(int page, int size, String sortType){
        //sortType은 최신글 순, 오래된 글 순, 좋아요, 조회수 총 6가지
        Sort sort;

        switch (sortType) {
            case "oldest": // 오래된 글 순
                sort = Sort.by("boardId").ascending(); // 오래된순은 boardId 기준 오름차순
                break;
            default:
                //기본값은 최신글 순
                sort = Sort.by("boardId").descending();
                break;
        }

        //쿼리문을 이용해 VisibilityStatus가 SECRET인 값들을 제외한 값들만 조회한다.
        //SELECT * FROM board WHERE visibility != 'SECRET' 와 비슷하다.

        return boardRepository.findByVisibilityNot(Board.VisibilityStatus.SECRET,PageRequest.of(page, size, sort));
    }
    //질문글 삭제
    public void deleteBoard(long boardId, long memberId){
        //삭제는 작성자만 가능해야 하며 관리자는 할 수 없기에 작성자인지만 검증한다.
        Board board = findVerifiedBoard(boardId);
        isBoardOwner(board, memberId);

        board.setBoardStauts(Board.BoardStauts.QUESTION_DELETED);
        boardRepository.save(board);
    }

    //작성자 여부 확인 메서드
    public void isBoardOwner(Board board, long memberId){
        //MemberService에 구현되어있는 회원 본인확인 메서드를 사용한다.
        memberService.isAuthenticatedMember(board.getMember().getMemberId(), memberId);
    }

    //비밀글 여부 확인 메서드
    public void isSecretBoard(Board board, long memberId){;
        if(board.getVisibility() == Board.VisibilityStatus.SECRET){
            //만약 관리자가 아닐경우 작성자인지 확인한다.
            if(!memberService.isAdmin(memberId)) {
                isBoardOwner(board, memberId);
            }
        }
    }

    //게시글 존재 여부 확인
    public Board findVerifiedBoard(long boardId){
        Optional<Board> optionalBoard = boardRepository.findById(boardId);
        Board board = optionalBoard.orElseThrow(()->
                new BusinessLogicException(ExceptionCode.BOARD_NOT_FOUND));

        return board;
    }
}
