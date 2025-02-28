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
        return savedBoard(board);
    }

    //질문글 수정
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public Board updateBoard(Board board, long memberId){
        //작성자(회원)이 실제 가입되어있는 회원인지 검증
        Member member = memberService.findVerifiedMember(memberId);

        //해당 게시판이 존재하는지 확인
        Board findBoard = findVerifiedBoard(board.getBoardId());

        //회원이 게시글의 작성자가 맞는지 확인
        //수정은 작성자, 관리자만 할 수있다.
        //만약 관리자가 아니라면 작성자인지 검증한다.
        if(!memberService.isAdmin(memberId)){
            isBoardOwner(findBoard, memberId);
        }

        Optional.ofNullable(board.getTitle())
                .ifPresent(title -> findBoard.setTitle(title));
        Optional.ofNullable(board.getContent())
                .ifPresent(content -> findBoard.setContent(content));
        Optional.ofNullable(board.getVisibility())
                .ifPresent(visibility -> findBoard.setVisibility(visibility));
//      상태수정은 불가능한게 맞다. 만약 삭제된 글인데 회원이 수정가능하면 안된다.
//      Optional.ofNullable(board.getBoardStauts())
//              .ifPresent(status -> board.setBoardStauts(status));

        //답변 완료 상태로의 수정은 관리자만 가능해야한다.
        //글 삭제, 글 비활성화, 답변완료 상태에서는 수정이 되어서는 안된다.
        if(!findBoard.getBoardStatus().equals(Board.BoardStatus.QUESTION_REGISTERED)){
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN_OPERATION);
        }

        return savedBoard(findBoard);
    }

    //특정 질문글 조회
    @Transactional(readOnly = true)
    public Board findBoard(long boardId, long memberId){
        Board board = findVerifiedBoard(boardId);
        //게시판이 비밀글인지 확인하여 비밀글이면 작성자, 관리자만 조회가능 하도록한다.
        isSecretBoard(board, memberId);
        //board.setViewCount(board.getViewCount() + 1);

        return board;
    }
    //전체 질문글 조회
    @Transactional(readOnly = true)
    public Page<Board> findBoards(int page, int size, String sortType, long memberId) {
        //sortType은 최신글 순, 오래된 글 순, 좋아요, 조회수 총 6가지
        Sort sort;

        switch (sortType) {
            case "oldest": // 오래된 글 순
                sort = Sort.by("boardId").ascending(); // 오래된순은 boardId 기준 오름차순
                break;
            case "likes": //좋아요 순
                sort = Sort.by("likeCount").descending();
                break;
            case "views": //조회수 순
                sort = Sort.by("viewCount").descending();
                break;
            default:
                //기본값은 최신글 순
                sort = Sort.by("boardId").descending();
                break;
        }

        //삭제되면 조회가 안되어야 하므로 삭제가 된 내용은 조회하지 못하게 한다.
        //근데 관리자는 알아야하지않을까?
        Page<Board> boards = boardRepository.findByBoardStatusNot(
                Board.BoardStatus.QUESTION_DELETED, PageRequest.of(page, size, sort));

        //boards에서 내용만 가져와 비밀글 유무를 파악하고 만약 비밀글이면 비밀글입니다로 출력되게 한다.
        //만약 관리자라면 삭제글, 비밀글도 보여주어야 한다.
        if(memberService.isAdmin(memberId)){
            return boardRepository.findAll(PageRequest.of(page,size,sort));
        }else {
            boards.getContent().forEach(board -> {
                if (board.getVisibility() == Board.VisibilityStatus.SECRET) {
                    board.setContent("비밀글입니다.");
                    board.setTitle("비밀글입니다.");
                    //질문 글이 비밀글인데, 답변이 달려있다고 한다면 답변글도 숨긴다.
                    if(board.getComment() != null){
                        board.getComment().setContent("해당 글은 비밀 글입니다.");
                    }
                }
            });
        }

        return boards;
        //쿼리문을 이용해 VisibilityStatus가 SECRET인 값들을 제외한 값들만 조회한다.
        //SELECT * FROM board WHERE visibility != 'SECRET' 와 비슷하다.
        //return boardRepository.findByVisibilityNot(Board.VisibilityStatus.SECRET,PageRequest.of(page, size, sort));
    }
    //질문글 삭제
    public void deleteBoard(long boardId, long memberId){
        //삭제는 작성자만 가능해야 하며 관리자는 할 수 없기에 작성자인지만 검증한다.
        Board board = findVerifiedBoard(boardId);
        isBoardOwner(board, memberId);

        board.setBoardStatus(Board.BoardStatus.QUESTION_DELETED);
        savedBoard(board);
    }

    public Board savedBoard(Board board){
        return boardRepository.save(board);
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

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    //조회수 증가 메서드
    public void viewCountUp(long boardId){
        //조회한 게시판을 찾는다.
        Board board = findVerifiedBoard(boardId);
        board.setViewCount(board.getViewCount() + 1);
    }
}
