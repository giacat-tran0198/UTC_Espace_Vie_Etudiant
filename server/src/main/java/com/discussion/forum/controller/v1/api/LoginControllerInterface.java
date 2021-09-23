package com.discussion.forum.controller.v1.api;

import com.discussion.forum.domain.User;
import com.discussion.forum.domain.vm.UserVM;
import com.discussion.forum.validation.CurrentUser;
import org.springframework.web.bind.annotation.PostMapping;

import static com.discussion.forum.utils.Constant.API_1_0_LOGIN;

public interface LoginControllerInterface {

    @PostMapping(API_1_0_LOGIN)
    UserVM handleLogin(@CurrentUser User user);
}
