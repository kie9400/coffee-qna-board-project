package com.springboot.member.controller;

import com.springboot.dto.MultiResponseDto;
import com.springboot.dto.SingleResponseDto;
import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.Member;
import com.springboot.member.mapper.MemberMapper;
import com.springboot.member.service.MemberService;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

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

    @PatchMapping("/{member-id}")
    public ResponseEntity patchMember(@PathVariable("member-id") @Positive long memberId,
                                      @Valid @RequestBody MemberDto.Patch memberPatchDto,
                                      @AuthenticationPrincipal Member authenticatedmember){
        memberPatchDto.setMemberId(memberId);
        Member member = memberService.updateMember(mapper.memberPatchToMember(memberPatchDto), authenticatedmember.getMemberId());

        return new ResponseEntity<>(new SingleResponseDto<>(mapper.memberToMemberResponse(member)), HttpStatus.OK);
    }

    @GetMapping("/{member-id}")
    public ResponseEntity getMember(@PathVariable("member-id") @Positive long memberId){
        Member member = memberService.findMember(memberId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.memberToMemberResponse(member)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getMembers(@Positive @RequestParam int page,
                                     @Positive @RequestParam int size){
        Page<Member> memberPage = memberService.findMembers(page - 1, size);
        List<Member> members = memberPage.getContent();
        return new ResponseEntity<>
                (new MultiResponseDto<>
                        (mapper.membersToMemberResponses(members),memberPage),HttpStatus.OK);
    }

    @DeleteMapping("/{member-id}")
    public ResponseEntity deleteMember(@PathVariable("member-id") @Positive long memberId,
                                       @AuthenticationPrincipal Member authenticatedmember){
        memberService.deleteMember(memberId, authenticatedmember.getMemberId());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
