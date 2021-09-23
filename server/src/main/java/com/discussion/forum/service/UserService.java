package com.discussion.forum.service;

import com.discussion.forum.domain.User;
import com.discussion.forum.domain.vm.UserUpdateVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User save(User user);
    Page<User> getUsers(User loggedInUser, Pageable pageable);
    User getByUsername(String username);
    User update(long id, UserUpdateVM userUpdate);
}
