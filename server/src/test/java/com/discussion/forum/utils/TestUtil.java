package com.discussion.forum.utils;

import com.discussion.forum.domain.Discussion;
import com.discussion.forum.domain.User;

public class TestUtil {
    public static String USERNAME = "test-user";
    public static String DISPLAYNAME = "test-display";
    public static String PASSWORD = "Password123";
    public static String IMAGE = "profile-image.png";

    public static User createValidUser() {
        User user = new User();
        user.setUsername(USERNAME);
        user.setDisplayName(DISPLAYNAME);
        user.setPassword(PASSWORD);
        user.setImage(IMAGE);
        return user;
    }

    public static User createValidUser(String username) {
        User user = createValidUser();
        user.setUsername(username);
        return user;
    }

    public static Discussion createValidDiscussion() {
        Discussion discussion = new Discussion();
        discussion.setContent("test content for the test discussion");
        return discussion;
    }

}
