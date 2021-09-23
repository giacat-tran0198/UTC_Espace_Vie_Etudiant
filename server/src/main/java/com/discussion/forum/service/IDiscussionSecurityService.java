package com.discussion.forum.service;

import com.discussion.forum.domain.User;

public interface IDiscussionSecurityService {
    boolean isAllowedToDelete(long hoaxId, User loggedInUser);
}
