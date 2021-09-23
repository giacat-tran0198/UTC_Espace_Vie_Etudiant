package com.discussion.forum.controller;

import com.discussion.forum.configuration.AppConfiguration;
import com.discussion.forum.domain.User;
import com.discussion.forum.domain.vm.UserUpdateVM;
import com.discussion.forum.domain.vm.UserVM;
import com.discussion.forum.exception.ApiErrorException;
import com.discussion.forum.repository.UserRepository;
import com.discussion.forum.service.UserService;
import com.discussion.forum.utils.TestPage;
import com.discussion.forum.utils.TestUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.discussion.forum.utils.Constant.API_1_0_USERS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    AppConfiguration appConfiguration;

    @BeforeEach
    public void cleanup() {
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    @AfterEach
    public void cleanDirectory() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    public <T> ResponseEntity<T> postSignup(Object request, Class<T> response) {
        return testRestTemplate.postForEntity(API_1_0_USERS, request, response);
    }

    public <T> ResponseEntity<T> getUsers(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_USERS, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getUsers(String path, ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getUser(String username, Class<T> responseType) {
        String path = API_1_0_USERS + "/" + username;
        return testRestTemplate.getForEntity(path, responseType);
    }

    public <T> ResponseEntity<T> putUser(long id, HttpEntity<?> requestEntity, Class<T> responseType) {
        String path = API_1_0_USERS + "/" + id;
        return testRestTemplate.exchange(path, HttpMethod.PUT, requestEntity, responseType);
    }

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, TestUtil.PASSWORD));
    }

    private UserUpdateVM createValidUserUpdateVM() {
        UserUpdateVM updatedUser = new UserUpdateVM();
        updatedUser.setDisplayName("newDisplayName");
        return updatedUser;
    }

    private String readFileToBase64(String fileName) throws IOException {
        ClassPathResource imageResource = new ClassPathResource(fileName);
        byte[] imageArr = FileUtils.readFileToByteArray(imageResource.getFile());
        return Base64.getEncoder().encodeToString(imageArr);
    }

    @Test
    public void postUser_whenUserIsValid_recevoirOk() {
        User user = TestUtil.createValidUser();
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserIsValid_userSavedToDB() {
        User user = TestUtil.createValidUser();
        postSignup(user, Object.class);
        Assertions.assertEquals(1, userRepository.count());
    }

    @Test
    public void postUser_whenUserIsValid_passwordIsHashedInDatabase() {
        User user = TestUtil.createValidUser();
        postSignup(user, Object.class);
        List<User> userInDB = userRepository.findAll();
        Assertions.assertNotEquals(user.getPassword(), userInDB.get(0).getPassword());
    }

    @Test
    public void postUser_whenUserHasNullUsername_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setUsername(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserHasNullDisplayName_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setDisplayName(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserHasNullPassword_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword(null);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserHasUsernameWithLessThanRequired_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setUsername("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserHasDisplayNameWithLessThanRequired_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setDisplayName("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserHasPasswordWithLessThanRequired_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword("abc");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserHasUsernameExceedsTheLengthLimit_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        String valueOf32Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setUsername(valueOf32Chars);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserHasDisplayNameExceedsTheLengthLimit_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        String valueOf32Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setDisplayName(valueOf32Chars);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserHasPasswordExceedsTheLengthLimit_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        String valueOf32Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setPassword(valueOf32Chars);
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserHasPasswordWithAllLowercase_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword("allowercase");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserHasPasswordWithAllUppercase_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword("ALLUPPERCASE");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserHasPasswordWithAllNumber_receiveBadRequest() {
        User user = TestUtil.createValidUser();
        user.setPassword("123456789");
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postUser_whenUserIsInvalid_receiveApiError() {
        User user = new User();
        ResponseEntity<ApiErrorException> response = postSignup(user, ApiErrorException.class);
        Assertions.assertEquals(API_1_0_USERS, Objects.requireNonNull(response.getBody()).getUrl());
    }

    @Test
    public void postUser_whenUserIsInvalid_receiveApiErrorWithValidationErrors() {
        User user = new User();
        ResponseEntity<ApiErrorException> response = postSignup(user, ApiErrorException.class);
        Assertions.assertEquals(3, Objects.requireNonNull(response.getBody()).getValidationErrors().size());
    }

    @Test
    public void postUser_whenAnotherUserHasSameUsername_receiveBadRequest() {
        userRepository.save(TestUtil.createValidUser());
        User user = TestUtil.createValidUser();
        ResponseEntity<Object> response = postSignup(user, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getUsers_whenThereAreNoUsersInDB_receiveOK() {
        ResponseEntity<Object> response = getUsers(new ParameterizedTypeReference<Object>() {
        });
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getUsers_whenThereAreNoUsersInDB_receivePageWithZeroItems() {
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        Assertions.assertEquals(0, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void getUsers_whenThereIsAUserInDB_receivePageWithUser() {
        userRepository.save(TestUtil.createValidUser());
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        Assertions.assertEquals(1, Objects.requireNonNull(response.getBody()).getNumberOfElements());
    }

    @Test
    public void getUsers_whenThereIsAUserInDB_receiveUserWithoutPassword() {
        userRepository.save(TestUtil.createValidUser());
        ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {
        });
        Map<String, Object> entity = Objects.requireNonNull(response.getBody()).getContent().get(0);
        Assertions.assertFalse(entity.containsKey("password"));
    }

    @Test
    public void getUsers_whenPageIsRequestedFor3ItemsPerPageWhereTheDatabaseHas20Users_receive3Users() {
        IntStream.rangeClosed(1, 20).mapToObj(i -> "test-user-" + i)
                .map(TestUtil::createValidUser)
                .forEach(userRepository::save);
        String path = API_1_0_USERS + "?page=0&size=3";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        Assertions.assertEquals(3, Objects.requireNonNull(response.getBody()).getContent().size());
    }

    @Test
    public void getUsers_whenPageSizeNotProvided_receivePageSizeAs10() {
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        Assertions.assertEquals(10, Objects.requireNonNull(response.getBody()).getSize());
    }

    @Test
    public void getUsers_whenPageSizeIsGreaterThan100_receivePageSizeAs100() {
        String path = API_1_0_USERS + "?size=500";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        Assertions.assertEquals(100, Objects.requireNonNull(response.getBody()).getSize());
    }

    @Test
    public void getUsers_whenPageSizeIsNegative_receivePageSizeAs10() {
        String path = API_1_0_USERS + "?size=-5";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        Assertions.assertEquals(10, Objects.requireNonNull(response.getBody()).getSize());
    }

    @Test
    public void getUsers_whenPageIsNegative_receiveFirstPage() {
        String path = API_1_0_USERS + "?page=-5";
        ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {
        });
        Assertions.assertEquals(0, Objects.requireNonNull(response.getBody()).getNumber());
    }

    @Test
    public void getUsers_whenUserLoggedIn_receivePageWithouLoggedInUser() {
        userService.save(TestUtil.createValidUser("user1"));
        userService.save(TestUtil.createValidUser("user2"));
        userService.save(TestUtil.createValidUser("user3"));
        authenticate("user1");
        ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {
        });
        Assertions.assertEquals(2, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void getUserByUsername_whenUserExist_receiveOk() {
        String username = "test-user";
        userService.save(TestUtil.createValidUser(username));
        ResponseEntity<Object> response = getUser(username, Object.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getUserByUsername_whenUserExist_receiveUserWithoutPassword() {
        String username = "test-user";
        userService.save(TestUtil.createValidUser(username));
        ResponseEntity<String> response = getUser(username, String.class);
        Assertions.assertFalse(Objects.requireNonNull(response.getBody()).contains("password"));
    }

    @Test
    public void getUserByUsername_whenUserDoesNotExist_receiveNotFound() {
        ResponseEntity<Object> response = getUser("unknown-user", Object.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getUserByUsername_whenUserDoesNotExist_receiveApiError() {
        ResponseEntity<ApiErrorException> response = getUser("unknown-user", ApiErrorException.class);
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).getMessage().contains("unknown-use"));
    }

    @Test
    public void putUser_whenUnauthorizedUserSendsTheRequest_receiveUnauthorized() {
        ResponseEntity<Object> response = putUser(123, null, Object.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void putUser_whenAuthorizedUserSendsUpdateForAnotherUser_receiveForbidden() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());

        long anotherUserId = user.getId() + 123;
        ResponseEntity<Object> response = putUser(anotherUserId, null, Object.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void putUser_whenUnauthorizedUserSendsTheRequest_receiveApiError() {
        ResponseEntity<ApiErrorException> response = putUser(123, null, ApiErrorException.class);
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).getUrl().contains("users/123"));
    }

    @Test
    public void putUser_whenAuthorizedUserSendsUpdateForAnotherUser_receiveApiError() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());

        long anotherUserId = user.getId() + 123;
        ResponseEntity<ApiErrorException> response = putUser(anotherUserId, null, ApiErrorException.class);
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).getUrl().contains("users/" + anotherUserId));
    }

    @Test
    public void putUser_whenValidRequestBodyFromAuthorizedUser_receiveOk() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void putUser_whenValidRequestBodyFromAuthorizedUser_displayNameUpdated() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        putUser(user.getId(), requestEntity, Object.class);

        Optional<User> userInDB = userRepository.findByUsername("user1");
        Assertions.assertEquals(updatedUser.getDisplayName(), userInDB.get().getDisplayName());
    }

    @Test
    public void putUser_whenValidRequestBodyFromAuthorizedUser_receiveUserVMWithUpdatedDisplayName() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        Assertions.assertEquals(updatedUser.getDisplayName(), Objects.requireNonNull(response.getBody()).getDisplayName());
    }

    @Test
    public void putUser_withValidRequestBodyWithSupportedImageFromAuthorizedUser_receiveUserVMWithRandomImageName() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();
        String imageString = readFileToBase64("profile.png");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        Assertions.assertNotEquals("profile-image.png", Objects.requireNonNull(response.getBody()).getImage());
    }

    @Test
    public void putUser_withValidRequestBodyWithSupportedImageFromAuthorizedUser_imageIsStoredUnderProfileFolder() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();

        String imageString = readFileToBase64("profile.png");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        String storedImageName = Objects.requireNonNull(response.getBody()).getImage();

        String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storedImageName;

        File storedImage = new File(profilePicturePath);
        Assertions.assertTrue(storedImage.exists());
    }

    @Test
    public void putUser_withInvalidRequestBodyWithNullDisplayNameFromAuthorizedUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = new UserUpdateVM();

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void putUser_withInvalidRequestBodyWithLessThanMinSizeDisplayNameFromAuthorizedUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = new UserUpdateVM();
        updatedUser.setDisplayName("abc");

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void putUser_withInvalidRequestBodyWithMoreThanMaxSizeDisplayNameFromAuthorizedUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = new UserUpdateVM();

        String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        updatedUser.setDisplayName(valueOf256Chars);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void putUser_withValidRequestBodyWithJPGImageFromAuthorizedUser_receiveOk() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();
        String imageString = readFileToBase64("test-jpg.jpg");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void putUser_withValidRequestBodyWithGIFImageFromAuthorizedUser_receiveBadRequest() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();
        String imageString = readFileToBase64("test-gif.gif");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void putUser_withValidRequestBodyWithTXTImageFromAuthorizedUser_receiveValidationErrorForProfileImage() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();
        String imageString = readFileToBase64("test-txt.txt");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<ApiErrorException> response = putUser(user.getId(), requestEntity, ApiErrorException.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        Assertions.assertEquals("Only PNG and JPG files are allowed", validationErrors.get("image"));
    }

    @Test
    public void putUser_withValidRequestBodyWithJPGImageForUserWhoHasImage_removesOldImageFromStorage() throws IOException {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate(user.getUsername());
        UserUpdateVM updatedUser = createValidUserUpdateVM();
        String imageString = readFileToBase64("test-jpg.jpg");
        updatedUser.setImage(imageString);

        HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);

        putUser(user.getId(), requestEntity, UserVM.class);

        String storedImageName = Objects.requireNonNull(response.getBody()).getImage();
        String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storedImageName;
        File storedImage = new File(profilePicturePath);
        Assertions.assertFalse(storedImage.exists());
    }
}
