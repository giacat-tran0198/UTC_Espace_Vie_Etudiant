package com.discussion.forum.controller.v1;

import com.discussion.forum.controller.v1.api.LoginControllerInterface;
import com.discussion.forum.domain.User;
import com.discussion.forum.domain.vm.UserVM;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController implements LoginControllerInterface {

    @Override
    public UserVM handleLogin(User user) {
        return new UserVM(user);
    }
}
