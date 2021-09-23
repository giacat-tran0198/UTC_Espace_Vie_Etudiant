package com.discussion.forum.controller.v1;

import com.discussion.forum.controller.v1.api.DiscussionUserControllerInterface;
import com.discussion.forum.domain.vm.DiscussionVM;
import com.discussion.forum.service.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DiscussionUserController implements DiscussionUserControllerInterface {

    @Autowired
    DiscussionService discussionService;

    @Override
    public ResponseEntity<?> getDiscussionsRelative(long id, String username, Pageable pageable, String direction, boolean count) {
        if (!direction.equalsIgnoreCase("after")) {
            return ResponseEntity.ok(discussionService.getOldDiscussions(id, username, pageable).map(DiscussionVM::new));
        }
        if (count) {
            long newDiscussionCount = discussionService.getNewDiscussionsCount(id, username);
            return ResponseEntity.ok(Collections.singletonMap("count", newDiscussionCount));
        }
        List<DiscussionVM> newDiscussions = discussionService.getNewDiscussions(id, username, pageable).stream()
                .map(DiscussionVM::new).collect(Collectors.toList());
        return ResponseEntity.ok(newDiscussions);
    }
}
