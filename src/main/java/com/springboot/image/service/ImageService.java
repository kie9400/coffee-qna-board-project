package com.springboot.image.service;

import com.springboot.board.entity.Board;
import com.springboot.board.service.BoardService;
import com.springboot.image.entity.Image;
import com.springboot.image.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Transactional
@Service
public class ImageService {
    @Value("${file.upload.path}")
    private String uploadPath;

    private final ImageRepository imageRepository;
    private final BoardService boardService;

    public ImageService(ImageRepository imageRepository, BoardService boardService) {
        this.imageRepository = imageRepository;
        this.boardService = boardService;
    }

    public void uploadFile(long boardId, MultipartFile file) throws IOException {
        Board board = boardService.findVerifiedBoard(boardId);

        // 실행 경로 기준으로 src/main/resources/filestorage 설정
        String uploadDir = System.getProperty("user.dir") + "/src/main/resources/filestorage";
        // 폴더가 존재하지 않는다면 생성한다.
        Files.createDirectories(Paths.get(uploadPath));

        // 중복 방지를 위해 UUID(유일한 식별자)를 추가
        // 파일명을 랜덤이름으로 변환하여 서버에 저장한다.
        // 언더바를 기준으로 뒤에 있는 이름이 파일명이므로 파일명을 헷갈릴 수 없음
        String imageName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, imageName);

        if (!imageName.matches(".*\\.(jpg|jpeg|png|gif)$")) {
            throw new IllegalArgumentException("Only Image File Upload");
        }

        // 실제 서버에 파일을 저장하기 위한 코드
        // MultipartFile에서 파일의 입력 스트림을 가져온다.
        // path는 저장할 파일의 경로 및 파일명을 지정한 객체이다.
        // 같은 파일의 이름이 이미 있으면 덮어씌운다.
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);


        // 파일을 지정된 경로에 저장
        Image image = new Image();
        image.setImageName(imageName);
        image.setImagePath(path.toString());
        image.setImageSize(file.getSize());
        image.setImageType(file.getContentType());
        image.setBoard(board);

        imageRepository.save(image);
    }
}
