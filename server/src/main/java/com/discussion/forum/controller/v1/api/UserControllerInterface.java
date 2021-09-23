package com.discussion.forum.controller.v1.api;

import com.discussion.forum.domain.User;
import com.discussion.forum.domain.vm.DiscussionVM;
import com.discussion.forum.domain.vm.UserUpdateVM;
import com.discussion.forum.domain.vm.UserVM;
import com.discussion.forum.validation.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.discussion.forum.utils.Constant.API_1_0_USERS;

@RequestMapping(API_1_0_USERS)
public interface UserControllerInterface {

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<String> createUser(@Valid @RequestBody User user);

    @GetMapping()
    Page<UserVM> getUsers(@CurrentUser User loggedInUser, Pageable page);

    @GetMapping("/{username}")
    UserVM getUserByName(@PathVariable String username);

    @PutMapping("/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
    UserVM updateUser(@PathVariable long id, @Valid @RequestBody(required = false) UserUpdateVM userUpdate);

    @GetMapping("/{username}/discussions")
    Page<DiscussionVM> getDiscussionsOfUser(@PathVariable String username, Pageable pageable);

}
