package com.springboot.comment.service;

import com.springboot.board.entity.Board;
import com.springboot.board.service.BoardService;
import com.springboot.comment.entity.Comment;
import com.springboot.comment.repository.CommentRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberService memberService;
    private final BoardService boardService;

    public CommentService(CommentRepository commentRepository, MemberService memberService, BoardService boardService) {
        this.commentRepository = commentRepository;
        this.memberService = memberService;
        this.boardService = boardService;
    }

    //답변글 등록
    public Comment createComment(long boardId, Comment comment, long memberId){
        //존재하는 계정인지 확인한다.
        Member findMember = memberService.findVerifiedMember(memberId);

        //인증된 회원이 관리자인지 확인한다.
        isAdminCheck(findMember.getMemberId());
        comment.setMember(findMember);

        //작성할 게시판 찾기
        Board findBoard = boardService.findVerifiedBoard(boardId);

        //이미 답변을 작성한건지 확인한다. ( 중복여부 확인 )
        verifyExistsComment(findBoard);

        findBoard.setComment(comment);

        //게시판에 답변을 등록하기 위해 적용 (단방향 관계이므로 수동으로 저장해준다.)
        //boardService.savedBoard(findBoard);

        return commentRepository.save(comment);
    }

    //답변글 수정 (게시판 정보, 수정하려는 정보, 회원 정보)인자로 받아옴
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public Comment updateComment(long boardId, Comment comment, long memberId){
        //인증된 회원이 관리자인지 확인한다.
        isAdminCheck(memberId);

        //수정하기위한 게시글에 답변이 존재하는지 확인
        Board findBoard = boardService.findVerifiedBoard(boardId);
        Comment findComment = findVerifiedComment(findBoard.getComment().getCommentId());
        Optional.ofNullable(comment.getContent())
                .ifPresent(content -> findComment.setContent(content));

        return commentRepository.save(findComment);
    }

    //답변글 삭제
    public void deleteComment(long boardId, long memberId){
        //인증된 회원이 관리자인지 확인한다.
        isAdminCheck(memberId);

        //게시판이 존재하는지 찾는다.
        Board board = boardService.findVerifiedBoard(boardId);
        //게시글에 있는 삭제할 답변을 찾는다.
        Comment findComment = findVerifiedComment(board.getComment().getCommentId());

        //연관관계의 주인인 board와의 연관관계를 끊어야 comment의 delete가 정상적으로 적용된다.
        //양방향 매핑을 이용하면 애너테이션으로 간단하게 가능! 하지만 필요할 때만 사용해야 한다.
        board.setComment(null);
        commentRepository.delete(findComment);
    }

    //해당 글에 이미 답변이 등록되었는지 검증
    public void verifyExistsComment(Board board){
        //만약 답변이 등록되어 있지 않다면 바로 return한다.
        if(board.getComment() == null){
            return;
        }

        Optional<Comment> optionalComment = commentRepository.findById(board.getComment().getCommentId());

        //이미 답변이 저장되어있다면 true -> 예외를 던진다.
        if (optionalComment.isPresent())
            throw new BusinessLogicException(ExceptionCode.COMMENT_EXISTS);
    }


    //답변이 존재하는지 찾기위한 메서드
    public Comment findVerifiedComment(long commentId){
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        Comment comment = optionalComment.orElseThrow( () ->
                new BusinessLogicException(ExceptionCode.COMMENT_NOT_FOUND));

        return comment;
    }


    public void isAdminCheck(long memberId){
        if(!memberService.isAdmin(memberId)){
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN_OPERATION);
        }
    }
}
