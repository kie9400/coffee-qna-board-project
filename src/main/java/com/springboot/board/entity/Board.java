package com.springboot.board.entity;

import com.springboot.audit.TimeStampedEntity;
import com.springboot.comment.entity.Comment;
import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class Board extends TimeStampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private BoardStauts boardStauts = BoardStauts.QUESTION_ANSWERED;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private VisibilityStatus visibilityStatus = VisibilityStatus.PUBLIC;

    //게시판에서는 답변을 조회할 수 있어야하기에 단방향 매핑
    @OneToOne
    @JoinColumn(name = "COMMENT_ID")
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    public void setMember(Member member){
        this.member = member;
        if(!member.getBoards().contains(this)){
            member.setBoard(this);
        }
    }

    public enum VisibilityStatus{
        PUBLIC(1, "공개글 상태"),
        SECRET(2, "비밀글 상태");

        //분기 처리를 위해 사용할 변수선언
        @Getter
        private int statusNumber;

        //현재 상태를 설명하기 위해 변수선언
        @Getter
        private String statusDescription;

        VisibilityStatus(int statusNumber, String statusDescription) {
            this.statusNumber = statusNumber;
            this.statusDescription = statusDescription;
        }
    }

    public enum BoardStauts{
        QUESTION_REGISTERED("질문 등록 상태"),
        QUESTION_ANSWERED("답변 완료 상태"),
        QUESTION_DELETED("질문 삭제 상태"),
        QUESTION_DEACTIVED("질문 비활성화 상태");

        @Getter
        private String status;

        BoardStauts(String status) {
            this.status = status;
        }

    }
}
