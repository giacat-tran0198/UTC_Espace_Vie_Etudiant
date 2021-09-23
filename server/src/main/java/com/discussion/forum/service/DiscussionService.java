package com.discussion.forum.service;

import com.discussion.forum.domain.Discussion;
import com.discussion.forum.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiscussionService {
    Discussion save(User user, Discussion discussion);

    Page<Discussion> getAllDiscussions(Pageable pageable);

    Page<Discussion> getDiscussionsOfUser(String username, Pageable pageable);

    Page<Discussion> getOldDiscussions(long id, String username, Pageable pageable);

    List<Discussion> getNewDiscussions(long id, String username, Pageable pageable);

    long getNewDiscussionsCount(long id, String username);

    void deleteDiscussion(long id);
}
