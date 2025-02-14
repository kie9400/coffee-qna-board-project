package com.springboot.comment.entity;

import com.springboot.audit.TimeStampedEntity;
import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Comment extends TimeStampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(nullable = false, length = 2000)
    private String content;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentStauts commentStauts = CommentStauts.COMMENT_REGISTERED;

    public void setMember(Member member){
        this.member = member;
        if(!member.getComments().contains(this)){
            member.setComment(this);
        }
    }

    //답변 상태는 필요없다. 글이 삭제되면 같이 삭제되고 글이 비공개면 같이 비공개이기 떄문
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
