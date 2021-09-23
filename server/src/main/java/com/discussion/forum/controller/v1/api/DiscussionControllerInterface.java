package com.discussion.forum.controller.v1.api;

import com.discussion.forum.domain.Discussion;
import com.discussion.forum.domain.User;
import com.discussion.forum.domain.vm.DiscussionVM;
import com.discussion.forum.validation.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.discussion.forum.utils.Constant.API_1_0_DISCUSSION;

@RequestMapping(API_1_0_DISCUSSION)
public interface DiscussionControllerInterface {

    @PostMapping()
    DiscussionVM createDiscussion(@Valid @RequestBody Discussion discussion, @CurrentUser User user);

    @GetMapping()
    Page<DiscussionVM> getAllDiscussions(Pageable pageable);

    @DeleteMapping("/{id:[0-9]+}")
    @PreAuthorize("@discussionSecurityService.isAllowedToDelete(#id, principal)")
    ResponseEntity<String> deleteDiscussion(@PathVariable long id);
}
