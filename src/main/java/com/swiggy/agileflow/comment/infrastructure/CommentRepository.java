package com.swiggy.agileflow.comment.infrastructure;

import com.swiggy.agileflow.comment.domain.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByIssueIdOrderByIdAsc(Long issueId);
}
