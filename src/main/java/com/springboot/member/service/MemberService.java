package com.springboot.member.service;

import com.springboot.auth.utils.AuthorityUtils;
import com.springboot.board.entity.Board;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityUtils authorityUtils;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, AuthorityUtils authorityUtils) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityUtils = authorityUtils;
    }

    public Member createMember(Member member){
        //중복 이메일 여부 확인
        verifyExistsEmail(member.getEmail());

        //패스워드 인코딩
        String encryptedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encryptedPassword);

        //권한 목록 저장
        List<String> roles = authorityUtils.createAuthorities(member.getEmail());
        member.setRoles(roles);

        return memberRepository.save(member);
    }

    //트랜잭션 전파 방식 설정으로 진행 중인 트랜잭션이 없는 경우 새로 시작한다.
    //진행 중이던 트랜잭션이 있다면 해당 트랜잭션에 참여한다.
    //트랜잭션 격리 레벨 설정으로 SERIALIZABLE은 동일한 데이터에 대해서 두 개 이상의 트랜잭션을 수행하지 못하도록 한다.
    //즉, 다른 트랜잭션을 차단시킨다. ( ex. patch가 두번 실행 되더라도 이 전 patch가 커밋되어야 다음 patch가 실행)
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public Member updateMember(Member member, long memberId){
        //멤버가 DB에 존재하는지 확인
        Member findMember = findVerifiedMember(member.getMemberId());

        //로그인한 멤버가 맞는지 확인
        isAuthenticatedMember(member.getMemberId(), memberId);

        //null처리를 위해서 Optional를 사용한다.
        Optional.ofNullable(member.getNickName())
                .ifPresent(nickName -> findMember.setNickName(nickName));

        return memberRepository.save(findMember);
    }

    //JPA 내부적으로 영속성 컨텍스트를 flush하지 않기 위해 readOnly=true로 설정한다.
    //읽기전용 트랜잭션이기에 flush 처리와 스냅샷을 생성하지 않아 불필요한 추가 동작을 줄인다(성능최적화)
    @Transactional(readOnly = true)
    public Member findMember(long memberId){
        //자기 자신만 조회가능? 요구조건에 없었으니 일단 pass
        return findVerifiedMember(memberId);
    }

    @Transactional(readOnly = true)
    public Page<Member> findMembers(int page, int size, long memberId){
        //관리자 인지 확인(관리자만 회원 전체를 조회할 수 있어야한다.)
        if(!isAdmin(memberId)){
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN_OPERATION);
        }

        //모든 회원을 페이지 단위로 받아 반환 (Page 객체를 반환한다.)
        //회원 목록을 페이지네이션 및 정렬하여 조회
        return memberRepository.findAll(PageRequest.of(page, size,
                Sort.by("memberId").descending()));
    }

    public void deleteMember(long memberid, long authenticationMemberId){
        //DB에 존재하는 회원인지 확인
        Member findMember = findVerifiedMember(memberid);
        //회원 본인이 맞는지 확인
        isAuthenticatedMember(memberid, authenticationMemberId);

        findMember.setMemberStatus(Member.MemberStatus.MEMBER_QUIT);
        findMember.getBoards().stream()
                        .forEach(board -> board.setBoardStatus(Board.BoardStatus.QUESTION_DEACTIVED));
        memberRepository.save(findMember);
    }

    //회원 본인이 맞는지 확인하기 위한 메서드
    public void isAuthenticatedMember(long memberId, long authenticationMemberId){
        if(memberId != authenticationMemberId){
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_MEMBER_ACCESS);
        }
    }

    //관리자 여부 확인 메서드
    public boolean isAdmin(long memberId){
        Member member = findVerifiedMember(memberId);
        return member.getRoles().contains("ADMIN");
    }

    public Member findVerifiedMember(long memberId){
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        Member member = optionalMember.orElseThrow(()->
                new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        return member;
    }

    public void verifyExistsEmail(String email){
        Optional<Member> member = memberRepository.findByEmail(email);

        //만약 멤버 객체가 값이 있다면(이메일이 존재한다면) true -> 예외를 던진다.
        if (member.isPresent())
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
    }
}
