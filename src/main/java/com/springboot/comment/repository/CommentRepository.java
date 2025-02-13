package com.springboot.comment.repository;

import com.springboot.comment.entity.Comment;
import org.mapstruct.Mapper;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
