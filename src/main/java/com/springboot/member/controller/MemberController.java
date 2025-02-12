package com.springboot.member.controller;

import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.Member;
import com.springboot.member.mapper.MemberMapper;
import com.springboot.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/members")
@Validated
public class MemberController {
    private final MemberService memberService;
    private final MemberMapper mapper;

    //서비스 계층과 데이터 엑세스계층 생성자로 DI주입
    public MemberController(MemberService memberService, MemberMapper mapper) {
        this.memberService = memberService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity postMember(@Valid @RequestBody MemberDto.Post memberPostDto){
        //요청(requestBody)를 받은 Dto를 mapper를 통해 엔티티로 변환
        //이후 서비스 계층을 통해 멤버를 생성
        Member member = memberService.createMember(mapper.memberPostToMember(memberPostDto));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
