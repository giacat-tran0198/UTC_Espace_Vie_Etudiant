package com.discussion.forum.service.impl;

import com.discussion.forum.domain.User;
import com.discussion.forum.domain.vm.UserUpdateVM;
import com.discussion.forum.exception.NotFoundException;
import com.discussion.forum.repository.UserRepository;
import com.discussion.forum.service.FileService;
import com.discussion.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
    }

    @Override
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Page<User> getUsers(User loggedInUser, Pageable pageable) {
        if (loggedInUser != null) {
            return userRepository.findByUsernameNot(loggedInUser.getUsername(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User getByUsername(String username) {
        Optional<User> inDB = userRepository.findByUsername(username);
        if (!inDB.isPresent()) {
            throw new NotFoundException(username + " not found");
        }
        return inDB.get();
    }

    @Override
    public User update(long id, UserUpdateVM userUpdate) {
        User inDB = userRepository.getById(id);
        inDB.setDisplayName(userUpdate.getDisplayName());
        if(userUpdate.getImage() != null) {
            String savedImageName;
            try {
                savedImageName = fileService.saveProfileImage(userUpdate.getImage());
                fileService.deleteProfileImage(inDB.getImage());
                inDB.setImage(savedImageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return userRepository.save(inDB);
    }
}