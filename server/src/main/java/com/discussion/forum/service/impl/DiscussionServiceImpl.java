package com.discussion.forum.service.impl;

import com.discussion.forum.domain.Discussion;
import com.discussion.forum.domain.FileAttachment;
import com.discussion.forum.domain.User;
import com.discussion.forum.repository.DiscussionRepository;
import com.discussion.forum.repository.FileAttachmentRepository;
import com.discussion.forum.service.DiscussionService;
import com.discussion.forum.service.FileService;
import com.discussion.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DiscussionServiceImpl implements DiscussionService {

    @Autowired
    DiscussionRepository discussionRepository;

    @Autowired
    UserService userService;

    @Autowired
    private FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    FileService fileService;

    @Override
    public Discussion save(User user, Discussion discussion) {
        discussion.setTimestamp(new Date());
        discussion.setUser(user);
        if(discussion.getAttachment() != null) {
            FileAttachment inDB = fileAttachmentRepository.findById(discussion.getAttachment().getId()).get();
            inDB.setDiscussion(discussion);
            discussion.setAttachment(inDB);
        }
        return discussionRepository.save(discussion);
    }

    @Override
    public Page<Discussion> getAllDiscussions(Pageable pageable) {
        return discussionRepository.findAll(pageable);
    }

    @Override
    public Page<Discussion> getDiscussionsOfUser(String username, Pageable pageable) {
        User inDB = userService.getByUsername(username);
        return discussionRepository.findByUser(inDB, pageable);
    }

    @Override
    public Page<Discussion> getOldDiscussions(long id, String username, Pageable pageable) {
        Specification<Discussion> spec = Specification.where(idLessThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return discussionRepository.findAll(spec, pageable);
    }

    @Override
    public List<Discussion> getNewDiscussions(long id, String username, Pageable pageable) {
        Specification<Discussion> spec = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return discussionRepository.findAll(spec, pageable.getSort());
    }

    @Override
    public long getNewDiscussionsCount(long id, String username) {
        Specification<Discussion> spec = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return discussionRepository.count(spec);
    }

    @Override
    public void deleteDiscussion(long id) {
        Discussion discussion = discussionRepository.getById(id);
        if(discussion.getAttachment() != null) {
            fileService.deleteAttachmentImage(discussion.getAttachment().getName());
        }
        discussionRepository.deleteById(id);
    }

    private Specification<Discussion> userIs(User user) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
    }

    private Specification<Discussion> idLessThan(long id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("id"), id);
    }

    private Specification<Discussion> idGreaterThan(long id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("id"), id);
    }
}
