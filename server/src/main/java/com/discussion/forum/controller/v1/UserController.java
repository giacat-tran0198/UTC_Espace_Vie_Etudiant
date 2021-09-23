package com.discussion.forum.controller.v1;

import com.discussion.forum.controller.v1.api.UserControllerInterface;
import com.discussion.forum.domain.User;
import com.discussion.forum.domain.vm.DiscussionVM;
import com.discussion.forum.domain.vm.UserUpdateVM;
import com.discussion.forum.domain.vm.UserVM;
import com.discussion.forum.service.DiscussionService;
import com.discussion.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController implements UserControllerInterface {

    @Autowired
    UserService userService;

    @Autowired
    DiscussionService discussionService;

    @Override
    public ResponseEntity<String> createUser(User user) {
        userService.save(user);
        return ResponseEntity.ok("User saved");
    }

    @Override
    public Page<UserVM> getUsers(User loggedInUser, Pageable page) {
        return userService.getUsers(loggedInUser, page).map(UserVM::new);
    }

    @Override
    public UserVM getUserByName(String username) {
        User user = userService.getByUsername(username);
        return new UserVM(user);
    }

    @Override
    public UserVM updateUser(long id, UserUpdateVM userUpdate) {
        User updated = userService.update(id, userUpdate);
        return new UserVM(updated);
    }

    @Override
    public Page<DiscussionVM> getDiscussionsOfUser(String username, Pageable pageable) {
        return discussionService.getDiscussionsOfUser(username, pageable).map(DiscussionVM::new);
    }

}
