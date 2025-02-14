package com.springboot.board.mapper;

import com.springboot.board.dto.BoardtDto;
import com.springboot.board.entity.Board;
import com.springboot.comment.dto.CommentDto;
import com.springboot.comment.entity.Comment;
import com.springboot.comment.mapper.CommentMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

//MapStruct가 Comment Mapper 클래스를 사용할 수 있도록 설정
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,uses = CommentMapper.class)
public interface BoardMapper {
    Board boardPostDtoToBoard(BoardtDto.Post requestBody);
    Board boardPatchDtoToBoard(BoardtDto.Patch requestBody);
    List<BoardtDto.Response> boardsToBoardResponsesDtos(List<Board> boards);

    default CommentDto.Response commentToCommentResponseDto(Comment comment, Board board){
        if(comment == null){
            return new CommentDto.Response("답변이 작성되지 않았습니다.", null);
        }

        CommentDto.Response responseDto = new CommentDto.Response(
          comment.getContent(),
          comment.getCreatedAt()
        );

        //글이 비공개 상태이고, 관리자가 아닐 경우 답변 내용을 감춘다.
        if (board.getVisibility() == Board.VisibilityStatus.SECRET
                & !board.getMember().getRoles().contains("ADMIN")) {
            responseDto.setContent("글이 비밀글 상태입니다.");
        }
        return responseDto;
    }

    //답변을 포함해서 Board 엔티티를 BoardtDto.Response 변환
    default BoardtDto.Response boardToBoardResponseDto(Board board){
        BoardtDto.Response responseDto = new BoardtDto.Response(
                board.getBoardId(),
                board.getTitle(),
                board.getContent(),
                board.getVisibility(),
                board.getBoardStatus(),
                board.getCreatedAt(),
                board.getModifiedAt(),
                commentToCommentResponseDto(board.getComment(), board)
        );
        return responseDto;
    }
}

