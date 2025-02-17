package com.springboot.image.entity;

import com.springboot.board.entity.Board;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Setter
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @Column(nullable = false)
    private String imageType; //파일형태

    @Column(nullable = false)
    private String imageName; // 파일명

    @Column(nullable = false)
    private String imagePath; // 파일 경로

    @Column(nullable = false)
    private Long imageSize; // 파일 크기

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;   // 게시글 ID
}
