package com.discussion.forum.repository;

import com.discussion.forum.domain.Discussion;
import com.discussion.forum.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DiscussionRepository extends JpaRepository<Discussion, Long>, JpaSpecificationExecutor<Discussion> {
    Page<Discussion> findByUser(User user, Pageable pageable);
}
