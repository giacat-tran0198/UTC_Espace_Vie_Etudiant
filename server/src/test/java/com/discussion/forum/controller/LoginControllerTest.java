package com.discussion.forum.controller;

import com.discussion.forum.domain.User;
import com.discussion.forum.exception.ApiErrorException;
import com.discussion.forum.repository.UserRepository;
import com.discussion.forum.service.UserService;
import com.discussion.forum.utils.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Objects;

import static com.discussion.forum.utils.Constant.API_1_0_LOGIN;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginControllerTest {
    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    public <T> ResponseEntity<T> login(Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_LOGIN, null, responseType);
    }

    public <T> ResponseEntity<T> login(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_LOGIN, HttpMethod.POST, null, responseType);
    }

    private void authenticate() {
        testRestTemplate
                .getRestTemplate()
                .getInterceptors()
                .add(new BasicAuthenticationInterceptor(TestUtil.USERNAME, TestUtil.PASSWORD));
    }

    @BeforeEach
    public void cleanup() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    @Test
    public void postLogin_withoutUserCredentials_receiveUnauthorized() {
        ResponseEntity<Object> response = login(Object.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void postLogin_withIncorrectCredentials_receiveUnauthorized() {
        authenticate();
        ResponseEntity<Object> response = login(Object.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void postLogin_withoutUserCredentials_receiveApiError() {
        ResponseEntity<ApiErrorException> response = login(ApiErrorException.class);
        Assertions.assertEquals(API_1_0_LOGIN, Objects.requireNonNull(response.getBody()).getUrl());
    }

    @Test
    public void postLogin_withoutUserCredentials_receiveApiErrorWithoutValidationErrors() {
        ResponseEntity<String> response = login(String.class);
        Assertions.assertFalse(Objects.requireNonNull(response.getBody()).contains("validationErrors"));
    }

    @Test
    public void postLogin_withIncorrectCredentials_receiveUnauthorizedWithoutWWWAuthenticationHeader() {
        authenticate();
        ResponseEntity<Object> response = login(Object.class);
        Assertions.assertFalse(response.getHeaders().containsKey("WWW-Authenticate"));
    }

    @Test
    public void postLogin_withValidCredentials_receiveOk() {
        userService.save(TestUtil.createValidUser());
        authenticate();
        ResponseEntity<Object> response = login(Object.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void postLogin_withValidCredentials_receiveLoggedInUserId() {
        User inDB = userService.save(TestUtil.createValidUser());
        authenticate();
        ResponseEntity<Map<String, Object>> response = login(new ParameterizedTypeReference<Map<String, Object>>() {
        });
        Map<String, Object> body = response.getBody();
        int id = (int) Objects.requireNonNull(body).get("id");
        Assertions.assertEquals(id, inDB.getId());
    }

    @Test
    public void postLogin_withValidCredentials_receiveLoggedInUsersImage() {
        User inDB = userService.save(TestUtil.createValidUser());
        authenticate();
        ResponseEntity<Map<String, Object>> response = login(new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<String, Object> body = response.getBody();
        String image = (String) Objects.requireNonNull(body).get("image");
        Assertions.assertEquals(image, inDB.getImage());
    }

    @Test
    public void postLogin_withValidCredentials_receiveLoggedInUsersDisplayName() {
        User inDB = userService.save(TestUtil.createValidUser());
        authenticate();
        ResponseEntity<Map<String, Object>> response = login(new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<String, Object> body = response.getBody();
        String displayName = (String) Objects.requireNonNull(body).get("displayName");
        Assertions.assertEquals(displayName,inDB.getDisplayName());
    }

    @Test
    public void postLogin_withValidCredentials_receiveLoggedInUsersUsername() {
        User inDB = userService.save(TestUtil.createValidUser());
        authenticate();
        ResponseEntity<Map<String, Object>> response = login(new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<String, Object> body = response.getBody();
        String username = (String) Objects.requireNonNull(body).get("username");
        Assertions.assertEquals(username,inDB.getUsername());
    }

    @Test
    public void postLogin_withValidCredentials_notReceiveLoggedInUsersPassword() {
        userService.save(TestUtil.createValidUser());
        authenticate();
        ResponseEntity<Map<String, Object>> response = login(new ParameterizedTypeReference<Map<String, Object>>() {});
        Map<String, Object> body = response.getBody();
        Assertions.assertFalse(Objects.requireNonNull(body).containsKey("password"));
    }
}
