package com.springboot.board.mapper;

import com.springboot.board.dto.BoardtDto;
import com.springboot.board.entity.Board;
import com.springboot.comment.dto.CommentDto;
import com.springboot.comment.entity.Comment;
import com.springboot.comment.mapper.CommentMapper;
import com.springboot.member.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

//MapStruct가 Comment Mapper 클래스를 사용할 수 있도록 설정
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
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
                commentToCommentResponseDto(board.getComment(), board),
                board.getLikeCount(),
                board.getViewCount()
        );

        return responseDto;
    }
}

