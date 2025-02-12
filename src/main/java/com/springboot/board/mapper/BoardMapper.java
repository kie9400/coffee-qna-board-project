package com.springboot.board.mapper;

import com.springboot.board.dto.BoardtDto;
import com.springboot.board.entity.Board;
import com.springboot.member.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BoardMapper {
    Board boardPostToBoard(BoardtDto.Post requestBody);
    Board boardPatchToBoard(BoardtDto.Patch requestBody);
    BoardtDto.Response boardToBoardResponse(Board board);
    List<BoardtDto.Response> boardsToBoardResponses(List<Board> boards);
}
