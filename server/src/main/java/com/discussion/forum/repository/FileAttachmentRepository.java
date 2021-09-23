package com.discussion.forum.repository;

import com.discussion.forum.domain.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
    List<FileAttachment> findByDateBeforeAndDiscussionIsNull(Date date);
}
