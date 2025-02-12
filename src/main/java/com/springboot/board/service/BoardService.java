package com.springboot.board.service;

import com.springboot.board.entity.Board;
import com.springboot.board.repository.BoardRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

        board.setMember(member);
        return boardRepository.save(board);
    }

    //회원이 존재하는지 확인
    private void verifyBoard(Board board){

    }
}
