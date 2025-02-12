package com.springboot.member.entity;

import com.springboot.board.entity.Board;
import com.springboot.comment.entity.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, updatable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 20)
    private String password;

    @Column(nullable = false, length = 13)
    private String nickName;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @Enumerated(value = EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MemberStatus memberStatus = MemberStatus.MEMBER_ACTIVE;

    //회원은 등록한 게시판과 답변의 정보를 조회할 수 있어야한다.
    @OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST)
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.PERSIST)
    private List<Comment> comments = new ArrayList<>();

    public void setComment(Comment comment){
        comments.add(comment);
        if(comment.getMember() != this){
            comment.setMember(this);
        }
    }

    public void setBoard(Board board) {
        boards.add(board);
        if (board.getMember() != this) {
            board.setMember(this);
        }
    }

    public enum MemberStatus {
        MEMBER_ACTIVE("활동 상태"),
        MEMBER_SLEEP("휴면 상태"),
        MEMBER_QUIT("탈퇴 상태");

        @Getter
        private String status;

        MemberStatus(String status) {
            this.status = status;
        }
    }
}
