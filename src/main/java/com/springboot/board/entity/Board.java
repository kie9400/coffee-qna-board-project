package com.springboot.board.entity;

import com.springboot.audit.TimeStampedEntity;
import com.springboot.comment.entity.Comment;
import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
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
    private BoardStatus boardStatus = BoardStatus.QUESTION_REGISTERED;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private VisibilityStatus visibility;

    //게시판에서는 답변을 조회할 수 있어야하기에 단방향 매핑
    //만약 답글이 삭제되면 연관된 질문글과의 참조를 제거해야한다.
    //자식 객체가 데이터베이스에서 삭제되고, 부모 객체의 외래키 필드도 null로 설정
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
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
        PUBLIC("공개글 상태"),
        SECRET("비밀글 상태");

        //현재 상태를 설명하기 위해 변수선언
        @Getter
        private String status;

        VisibilityStatus(String status) {
            this.status = status;
        }
    }

    public enum BoardStatus{
        QUESTION_REGISTERED("질문 등록 상태"),
        QUESTION_ANSWERED("답변 완료 상태"),
        QUESTION_DELETED("질문 삭제 상태"),
        QUESTION_DEACTIVED("질문 비활성화 상태");

        @Getter
        private String status;

        BoardStatus(String status) {
            this.status = status;
        }
    }
}
