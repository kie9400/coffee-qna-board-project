package com.springboot.like.repository;

import com.springboot.board.entity.Board;
import com.springboot.like.entity.Like;
import com.springboot.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    //사용자가 이미 게시판에 좋아요를 눌렀는지 검증(중복여부 검사)
    Optional<Like> findByMemberAndBoard(Member member, Board board);
}
