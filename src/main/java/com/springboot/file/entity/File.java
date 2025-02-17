package com.springboot.file.entity;

import com.springboot.board.entity.Board;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Setter
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    @Column(nullable = false)
    private String fileType; //파일형태

    @Column(nullable = false)
    private String fileName; // 파일명

    @Column(nullable = false)
    private String filePath; // 파일 경로

    @Column(nullable = false)
    private Long fileSize; // 파일 크기

    @Column(nullable = false)
    private Board board;   // 게시글 ID
}
