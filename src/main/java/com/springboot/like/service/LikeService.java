package com.springboot.like.service;

import com.springboot.board.entity.Board;
import com.springboot.board.service.BoardService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.like.entity.Like;
import com.springboot.like.repository.LikeRepository;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final MemberService memberService;
    private final BoardService boardService;

    public LikeService(LikeRepository likeRepository, MemberService memberService, BoardService boardService) {
        this.likeRepository = likeRepository;
        this.memberService = memberService;
        this.boardService = boardService;
    }

    public Like addLike(long boardId, long memberId) {
        //로그인한 회원인지 확인
        Member member = memberService.findVerifiedMember(memberId);

        //존재하는 게시판인지 확인
        Board board = boardService.findVerifiedBoard(boardId);

        //좋아요 중복 여부 검사
        //이미 해당 회원이 해당 게시판에 좋아요를 눌렀으면 예외처리
        if (likeRepository.findByMemberAndBoard(member, board).isPresent()) {
            throw new BusinessLogicException(ExceptionCode.LIKE_ALREADY_EXISTS);
        }
        board.setLikeCount(board.getLikeCount() + 1);
        return likeRepository.save(new Like(member, board));
    }

    public void deleteLike(long boardId, long memberId) {
        //로그인한 회원인지 확인
        Member member = memberService.findVerifiedMember(memberId);

        //존재하는 게시판인지 확인
        Board board = boardService.findVerifiedBoard(boardId);

        Like like = likeRepository.findByMemberAndBoard(member, board).orElseThrow(()->
                new BusinessLogicException(ExceptionCode.LIKE_NOT_CLICK));
        //좋아요를 누르지 않았을 경우 예외 발생

        board.setLikeCount(board.getLikeCount() - 1);
        likeRepository.delete(like);
    }
}
