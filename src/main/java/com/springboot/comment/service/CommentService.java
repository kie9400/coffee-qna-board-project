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

        //작성할 게시판 찾기
        Board findBoard = boardService.findVerifiedBoard(boardId);
        comment.setMember(findMember);

        findBoard.setComment(comment);
        boardService.savedBoard(findBoard);

        return commentRepository.save(comment);
    }

    //답변글 수정
//    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
//    public Comment updateComment(Comment comment, long memberId){
//
//    }

    //답변글 삭제
    public void deleteComment(long commentId, long memberId){

    }

    public void isAdminCheck(long memberId){
        if(!memberService.isAdmin(memberId)){
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN_OPERATION);
        }
    }
}
