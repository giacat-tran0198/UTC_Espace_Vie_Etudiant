package com.discussion.forum.controller.v1;

import com.discussion.forum.controller.v1.api.DiscussionControllerInterface;
import com.discussion.forum.controller.v1.api.DiscussionUserControllerInterface;
import com.discussion.forum.domain.Discussion;
import com.discussion.forum.domain.User;
import com.discussion.forum.domain.vm.DiscussionVM;
import com.discussion.forum.service.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DiscussionController implements DiscussionControllerInterface {

    @Autowired
    DiscussionService discussionService;

    @Override
    public DiscussionVM createDiscussion(Discussion discussion, User user) {
        return new DiscussionVM(discussionService.save(user, discussion));
    }

    @Override
    public Page<DiscussionVM> getAllDiscussions(Pageable pageable) {
        return discussionService.getAllDiscussions(pageable).map(DiscussionVM::new);
    }

    @Override
    public ResponseEntity<String> deleteDiscussion(long id) {
        discussionService.deleteDiscussion(id);
        return ResponseEntity.ok("Discussion is removed");
    }
}
