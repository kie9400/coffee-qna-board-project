package com.springboot.comment.entity;

import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentStauts commentStauts = CommentStauts.COMMENT_REGISTERED;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    public void setMember(Member member){
        this.member = member;
        if(!member.getComments().contains(this)){
            member.setComment(this);
        }
    }

    public enum CommentStauts{
        COMMENT_REGISTERED("답변 등록 상태"),
        COMMENT_DELETED("질문 삭제 상태"),;

        @Getter
        private String status;

        CommentStauts(String status) {
            this.status = status;
        }
    }
}
