package com.discussion.forum.service.impl;

import com.discussion.forum.domain.Discussion;
import com.discussion.forum.domain.User;
import com.discussion.forum.repository.DiscussionRepository;
import com.discussion.forum.service.IDiscussionSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DiscussionSecurityService implements IDiscussionSecurityService {

    @Autowired
    DiscussionRepository discussionRepository;

    public boolean isAllowedToDelete(long hoaxId, User loggedInUser) {
        Optional<Discussion> optionalHoax = discussionRepository.findById(hoaxId);
        if (optionalHoax.isPresent()) {
            Discussion inDB = optionalHoax.get();
            return inDB.getUser().getId() == loggedInUser.getId();
        }
        return false;
    }
}
