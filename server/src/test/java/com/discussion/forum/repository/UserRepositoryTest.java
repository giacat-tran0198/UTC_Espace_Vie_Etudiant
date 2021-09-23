package com.discussion.forum.repository;

import com.discussion.forum.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TestEntityManager testEntityManager;

    @BeforeEach
    public void cleanup() {
        userRepository.deleteAll();
    }

    private User createValidUser() {
        User user = new User();
        user.setUsername("test-user");
        user.setDisplayName("test-display");
        user.setPassword("Password123");
        return user;
    }

    @Test
    public void findByUserName_whenUserExists_returnUser() {
        User user = createValidUser();
        testEntityManager.persist(user);
        Optional<User> inDB = userRepository.findByUsername(user.getUsername());
        Assertions.assertNotNull(inDB);
    }

    @Test
    public void findByUserName_whenUserDoesNotExists_returnNull() {
        Optional<User> inDB = userRepository.findByUsername("test");
        Assertions.assertFalse(inDB.isPresent());
    }
}
