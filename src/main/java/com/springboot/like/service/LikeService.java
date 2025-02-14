package com.springboot.like.service;

import com.springboot.board.entity.Board;
import com.springboot.board.service.BoardService;
import com.springboot.like.entity.Like;
import com.springboot.like.repository.LikeRepository;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Like addLike(long boardId, long memberId){
        //로그인한 회원인지 확인
        Member member = memberService.findVerifiedMember(memberId);

        //존재하는 게시판인지 확인
        Board board = boardService.findVerifiedBoard(boardId);

        //좋아요 중복 여부 검사
        //만약 좋아요가 안눌러져있다면 저장한다. ( 좋아요수 1회 증가 )
        if(!likeRepository.existsByMemberAndBoard(member, board)){
            board.setLikeCount(board.getLikeCount() + 1);
            likeRepository.save(new Like(member, board));
        }
    }
}
