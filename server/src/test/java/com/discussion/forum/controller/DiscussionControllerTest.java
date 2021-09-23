package com.discussion.forum.controller;

import com.discussion.forum.configuration.AppConfiguration;
import com.discussion.forum.domain.Discussion;
import com.discussion.forum.domain.FileAttachment;
import com.discussion.forum.domain.User;
import com.discussion.forum.domain.vm.DiscussionVM;
import com.discussion.forum.exception.ApiErrorException;
import com.discussion.forum.repository.DiscussionRepository;
import com.discussion.forum.repository.FileAttachmentRepository;
import com.discussion.forum.repository.UserRepository;
import com.discussion.forum.service.DiscussionService;
import com.discussion.forum.service.FileService;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.discussion.forum.utils.Constant.API_1_0_DISCUSSION;
import static com.discussion.forum.utils.Constant.API_1_0_USERS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DiscussionControllerTest {
    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DiscussionRepository discussionRepository;

    @Autowired
    DiscussionService discussionService;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    FileService fileService;

    @Autowired
    AppConfiguration appConfiguration;


    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    public void cleanup() throws IOException {
        fileAttachmentRepository.deleteAll();
        discussionRepository.deleteAll();
        userRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @AfterEach
    public void cleanupAfter() {
        fileAttachmentRepository.deleteAll();
        discussionRepository.deleteAll();
    }

    private <T> ResponseEntity<T> postDiscussion(Discussion discussion, Class<T> responseType) {
        return testRestTemplate.postForEntity(API_1_0_DISCUSSION, discussion, responseType);
    }


    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, TestUtil.PASSWORD));
    }

    private MultipartFile createFile() throws IOException {
        ClassPathResource imageResource = new ClassPathResource("profile.png");
        byte[] fileAsByte = FileUtils.readFileToByteArray(imageResource.getFile());

        return new MockMultipartFile("profile.png", fileAsByte);
    }

    public <T> ResponseEntity<T> getDiscussions(ParameterizedTypeReference<T> responseType) {
        return testRestTemplate.exchange(API_1_0_DISCUSSION, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getDiscussionsOfUser(String username, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_USERS + "/" + username + "/discussions";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getOldDiscussions(long id, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_DISCUSSION + "/" + id + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getOldDiscussionsOfUser(long id, String username, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_USERS + "/" + username + "/discussions/" + id + "?direction=before&page=0&size=5&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewDiscussions(long id, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_DISCUSSION + "/" + id + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewDiscussionsOfUser(long id, String username, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_USERS + "/" + username + "/discussions/" + id + "?direction=after&sort=id,desc";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewDiscussionCount(long id, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_DISCUSSION + "/" + id + "?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> getNewDiscussionCountOfUser(long id, String username, ParameterizedTypeReference<T> responseType) {
        String path = API_1_0_USERS + "/" + username + "/discussions/" + id + "?direction=after&count=true";
        return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> ResponseEntity<T> deleteDiscussion(long discussionId, Class<T> responseType) {
        return testRestTemplate.exchange(API_1_0_DISCUSSION + "/" + discussionId, HttpMethod.DELETE, null, responseType);
    }

    @Test
    public void postDiscussion_whenDiscussionIsValidAndUserIsAuthorized_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = TestUtil.createValidDiscussion();
        ResponseEntity<Object> response = postDiscussion(discussion, Object.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void postDiscussion_whenDiscussionIsValidAndUserIsUnauthorized_receiveUnauthorized() {
        Discussion discussion = TestUtil.createValidDiscussion();
        ResponseEntity<Object> response = postDiscussion(discussion, Object.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void postDiscussion_whenDiscussionIsValidAndUserIsUnauthorized_receiveApiError() {
        Discussion discussion = TestUtil.createValidDiscussion();
        ResponseEntity<ApiErrorException> response = postDiscussion(discussion, ApiErrorException.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), Objects.requireNonNull(response.getBody()).getStatus());
    }

    @Test
    public void postDiscussion_whenDiscussionIsValidAndUserIsAuthorized_discussionSavedToDatabase() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = TestUtil.createValidDiscussion();
        postDiscussion(discussion, Object.class);

        Assertions.assertEquals(1, discussionRepository.count());
    }

    @Test
    public void postDiscussion_whenDiscussionIsValidAndUserIsAuthorized_discussionSavedToDatabaseWithTimestamp() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = TestUtil.createValidDiscussion();
        postDiscussion(discussion, Object.class);

        Discussion inDB = discussionRepository.findAll().get(0);

        Assertions.assertNotNull(inDB.getTimestamp());
    }

    @Test
    public void postDiscussion_whenDiscussionContentNullAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = new Discussion();
        ResponseEntity<Object> response = postDiscussion(discussion, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postDiscussion_whenDiscussionContentLessThan10CharactersAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = new Discussion();
        discussion.setContent("123456789");
        ResponseEntity<Object> response = postDiscussion(discussion, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void postDiscussion_whenDiscussionIsValidAndUserIsAuthorized_receiveDiscussionVM() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = TestUtil.createValidDiscussion();
        ResponseEntity<DiscussionVM> response = postDiscussion(discussion, DiscussionVM.class);
        Assertions.assertEquals("user1", response.getBody().getUser().getUsername());
    }

    @Test
    public void postDiscussion_whenDiscussionContentIs5000CharactersAndUserIsAuthorized_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = new Discussion();
        String veryLongString = IntStream.rangeClosed(1, 5000).mapToObj(i -> "x").collect(Collectors.joining());
        discussion.setContent(veryLongString);
        ResponseEntity<Object> response = postDiscussion(discussion, Object.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void postDiscussion_whenDiscussionContentMoreThan5000CharactersAndUserIsAuthorized_receiveBadRequest() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = new Discussion();
        String veryLongString = IntStream.rangeClosed(1, 5001).mapToObj(i -> "x").collect(Collectors.joining());
        discussion.setContent(veryLongString);
        ResponseEntity<Object> response = postDiscussion(discussion, Object.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    public void postDiscussion_whenDiscussionContentNullAndUserIsAuthorized_receiveApiErrorWithValidationErrors() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = new Discussion();
        ResponseEntity<ApiErrorException> response = postDiscussion(discussion, ApiErrorException.class);
        Map<String, String> validationErrors = Objects.requireNonNull(response.getBody()).getValidationErrors();
        Assertions.assertNotNull(validationErrors.get("content"));
    }

    @Test
    public void postDiscussion_whenDiscussionIsValidAndUserIsAuthorized_discussionSavedWithAuthenticatedUserInfo() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = TestUtil.createValidDiscussion();
        postDiscussion(discussion, Object.class);

        Discussion inDB = discussionRepository.findAll().get(0);
        Assertions.assertEquals("user1", inDB.getUser().getUsername());
    }

    @Test
    public void postDiscussion_whenDiscussionIsValidAndUserIsAuthorized_discussionCanBeAccessedFromUserEntity() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = TestUtil.createValidDiscussion();
        postDiscussion(discussion, Object.class);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        User inDBUser = entityManager.find(User.class, user.getId());

        Assertions.assertEquals(1, inDBUser.getDiscussions().size());

    }

    @Test
    public void getDiscussions_whenThereAreNoDiscussions_receiveOk() {
        ResponseEntity<Object> response = getDiscussions(new ParameterizedTypeReference<Object>() {
        });
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getDiscussions_whenThereAreNoDiscussions_receivePageWithZeroItems() {
        ResponseEntity<TestPage<Object>> response = getDiscussions(new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(0, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void getDiscussions_whenThereAreDiscussions_receivePageWithItems() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<TestPage<Object>> response = getDiscussions(new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(3, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void getDiscussions_whenThereAreDiscussions_receivePageWithDiscussionVM() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<TestPage<DiscussionVM>> response = getDiscussions(new ParameterizedTypeReference<>() {
        });
        DiscussionVM storedDiscussion = response.getBody().getContent().get(0);
        Assertions.assertEquals("user1", storedDiscussion.getUser().getUsername());
    }

    @Test
    public void getDiscussionsOfUser_whenUserExists_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getDiscussionsOfUser("user1", new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getDiscussionsOfUser_whenUserDoesNotExist_receiveNotFound() {
        ResponseEntity<Object> response = getDiscussionsOfUser("unknown-user", new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getDiscussionsOfUser_whenUserExists_receivePageWithZeroDiscussions() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<TestPage<Object>> response = getDiscussionsOfUser("user1", new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(0, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void getDiscussionsOfUser_whenUserExistWithDiscussions_receivePageWithDiscussionVM() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<TestPage<DiscussionVM>> response = getDiscussionsOfUser("user1", new ParameterizedTypeReference<>() {
        });
        DiscussionVM storedDiscussions = Objects.requireNonNull(response.getBody()).getContent().get(0);
        Assertions.assertEquals("user1", storedDiscussions.getUser().getUsername());
    }

    @Test
    public void getDiscussionsOfUser_whenUserExistWithMultipleDiscussions_receivePageWithMatchingDiscussionsCount() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<TestPage<DiscussionVM>> response = getDiscussionsOfUser("user1", new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(3, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void getDiscussionsOfUser_whenMultipleUserExistWithMultipleDiscussions_receivePageWithMatchingDiscussionsCount() {
        User userWithThreeDiscussions = userService.save(TestUtil.createValidUser("user1"));
        IntStream.rangeClosed(1, 3).forEach(i -> {
            discussionService.save(userWithThreeDiscussions, TestUtil.createValidDiscussion());
        });

        User userWithFiveDiscussions = userService.save(TestUtil.createValidUser("user2"));
        IntStream.rangeClosed(1, 5).forEach(i -> {
            discussionService.save(userWithFiveDiscussions, TestUtil.createValidDiscussion());
        });


        ResponseEntity<TestPage<DiscussionVM>> response = getDiscussionsOfUser(userWithFiveDiscussions.getUsername(), new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(5, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void getOldDiscussions_whenThereAreNoDiscussions_receiveOk() {
        ResponseEntity<Object> response = getOldDiscussions(5, new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getOldDiscussions_whenThereAreDiscussions_receivePageWithItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        Discussion fourth = discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<TestPage<Object>> response = getOldDiscussions(fourth.getId(), new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(3, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void getOldDiscussions_whenThereAreDiscussions_receivePageWithDiscussionsVMBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        Discussion fourth = discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<TestPage<DiscussionVM>> response = getOldDiscussions(fourth.getId(), new ParameterizedTypeReference<>() {
        });
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).getContent().get(0).getDate() > 0);
    }

    @Test
    public void getOldDiscussionsOfUser_whenUserExistThereAreNoDiscussions_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getOldDiscussionsOfUser(5, "user1", new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getOldDiscussionsOfUser_whenUserExistAndThereAreDiscussions_receivePageWithItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        Discussion fourth = discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<TestPage<Object>> response = getOldDiscussionsOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(3, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void getOldDiscussionsOfUser_whenUserExistAndThereAreDiscussions_receivePageWithDiscussionVMBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        Discussion fourth = discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<TestPage<DiscussionVM>> response = getOldDiscussionsOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<>() {
        });
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).getContent().get(0).getDate() > 0);
    }


    @Test
    public void getOldDiscussionsOfUser_whenUserDoesNotExistThereAreNoDiscussions_receiveNotFound() {
        ResponseEntity<Object> response = getOldDiscussionsOfUser(5, "user1", new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getOldDiscussionsOfUser_whenUserExistAndThereAreNoDiscussions_receivePageWithZeroItemsBeforeProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        Discussion fourth = discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        userService.save(TestUtil.createValidUser("user2"));

        ResponseEntity<TestPage<DiscussionVM>> response = getOldDiscussionsOfUser(fourth.getId(), "user2", new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(0, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void getNewDiscussions_whenThereAreDiscussions_receiveListOfItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        Discussion fourth = discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<List<Object>> response = getNewDiscussions(fourth.getId(), new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(1, Objects.requireNonNull(response.getBody()).size());
    }

    @Test
    public void getNewDiscussions_whenThereAreDiscussions_receiveListOfDiscussionVMAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        Discussion fourth = discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<List<DiscussionVM>> response = getNewDiscussions(fourth.getId(), new ParameterizedTypeReference<>() {
        });
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).get(0).getDate() > 0);
    }

    @Test
    public void getNewDiscussionsOfUser_whenUserExistThereAreNoDiscussions_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        ResponseEntity<Object> response = getNewDiscussionsOfUser(5, "user1", new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getNewDiscussionsOfUser_whenUserExistAndThereAreDiscussions_receiveListWithItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        Discussion fourth = discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<List<Object>> response = getNewDiscussionsOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(1, Objects.requireNonNull(response.getBody()).size());
    }

    @Test
    public void getNewDiscussionsOfUser_whenUserExistAndThereAreDiscussions_receiveListWithDiscussionVMAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        Discussion fourth = discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<List<DiscussionVM>> response = getNewDiscussionsOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<>() {
        });
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).get(0).getDate() > 0);
    }


    @Test
    public void getNewDiscussionsOfUser_whenUserDoesNotExistThereAreNoDiscussions_receiveNotFound() {
        ResponseEntity<Object> response = getNewDiscussionsOfUser(5, "user1", new ParameterizedTypeReference<Object>() {
        });
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getNewDiscussionsOfUser_whenUserExistAndThereAreNoDiscussions_receiveListWithZeroItemsAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        Discussion fourth = discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        userService.save(TestUtil.createValidUser("user2"));

        ResponseEntity<List<DiscussionVM>> response = getNewDiscussionsOfUser(fourth.getId(), "user2", new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(0, Objects.requireNonNull(response.getBody()).size());
    }

    @Test
    public void getNewDiscussionCount_whenThereAreDiscussions_receiveCountAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        Discussion fourth = discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<Map<String, Long>> response = getNewDiscussionCount(fourth.getId(), new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(1, Objects.requireNonNull(response.getBody()).get("count"));
    }


    @Test
    public void getNewDiscussionCountOfUser_whenThereAreDiscussions_receiveCountAfterProvidedId() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());
        Discussion fourth = discussionService.save(user, TestUtil.createValidDiscussion());
        discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<Map<String, Long>> response = getNewDiscussionCountOfUser(fourth.getId(), "user1", new ParameterizedTypeReference<>() {
        });
        Assertions.assertEquals(1, Objects.requireNonNull(response.getBody()).get("count"));
    }

    @Test
    public void postDiscussion_whenDiscussionHasFileAttachmentAndUserIsAuthorized_fileAttachmentDiscussionRelationIsUpdatedInDatabase() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Discussion discussion = TestUtil.createValidDiscussion();
        discussion.setAttachment(savedFile);
        ResponseEntity<DiscussionVM> response = postDiscussion(discussion, DiscussionVM.class);

        FileAttachment inDB = fileAttachmentRepository.findAll().get(0);
        Assertions.assertEquals(Objects.requireNonNull(response.getBody()).getId(), inDB.getDiscussion().getId());
    }

    @Test
    public void postDiscussion_whenDiscussionHasFileAttachmentAndUserIsAuthorized_discussionFileAttachmentRelationIsUpdatedInDatabase() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Discussion discussion = TestUtil.createValidDiscussion();
        discussion.setAttachment(savedFile);
        ResponseEntity<DiscussionVM> response = postDiscussion(discussion, DiscussionVM.class);

        Discussion inDB = discussionRepository.findById(Objects.requireNonNull(response.getBody()).getId()).get();
        Assertions.assertEquals(savedFile.getId(), inDB.getAttachment().getId());
    }

    @Test
    public void postDiscussion_whenDiscussionHasFileAttachmentAndUserIsAuthorized_receiveDiscussionVMWithAttachment() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Discussion discussion = TestUtil.createValidDiscussion();
        discussion.setAttachment(savedFile);
        ResponseEntity<DiscussionVM> response = postDiscussion(discussion, DiscussionVM.class);

        Assertions.assertEquals(savedFile.getName(), Objects.requireNonNull(response.getBody()).getAttachment().getName());
    }

    @Test
    public void deleteDiscussion_whenUserIsUnAuthorized_receiveUnauthorized() {
        ResponseEntity<Object> response = deleteDiscussion(555, Object.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void deleteDiscussion_whenUserIsAuthorized_receiveOk() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<String> response = deleteDiscussion(discussion.getId(), String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void deleteDiscussion_whenUserIsAuthorized_receiveGenericResponse() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = discussionService.save(user, TestUtil.createValidDiscussion());

        ResponseEntity<String> response = deleteDiscussion(discussion.getId(), String.class);
        Assertions.assertNotNull(response.getBody());

    }

    @Test
    public void deleteDiscussion_whenUserIsAuthorized_discussionRemovedFromDatabase() {
        User user = userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        Discussion discussion = discussionService.save(user, TestUtil.createValidDiscussion());

        deleteDiscussion(discussion.getId(), String.class);
        Optional<Discussion> inDB = discussionRepository.findById(discussion.getId());
        Assertions.assertFalse(inDB.isPresent());
    }

    @Test
    public void deleteDiscussion_whenDiscussionIsOwnedByAnotherUser_receiveForbidden() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        User discussionOwner = userService.save(TestUtil.createValidUser("discussion-owner"));
        Discussion discussion = discussionService.save(discussionOwner, TestUtil.createValidDiscussion());

        ResponseEntity<Object> response = deleteDiscussion(discussion.getId(), Object.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

    }

    @Test
    public void deleteDiscussion_whenDiscussionNotExist_receiveForbidden() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        ResponseEntity<Object> response = deleteDiscussion(5555, Object.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void deleteDiscussion_whenDiscussionHasAttachment_attachmentRemovedFromDatabase() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Discussion discussion = TestUtil.createValidDiscussion();
        discussion.setAttachment(savedFile);
        ResponseEntity<DiscussionVM> response = postDiscussion(discussion, DiscussionVM.class);

        long discussionId = response.getBody().getId();

        deleteDiscussion(discussionId, String.class);

        Optional<FileAttachment> optionalAttachment = fileAttachmentRepository.findById(savedFile.getId());

        Assertions.assertFalse(optionalAttachment.isPresent());
    }

    @Test
    public void deleteDiscussion_whenDiscussionHasAttachment_attachmentRemovedFromStorage() throws IOException {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");

        MultipartFile file = createFile();

        FileAttachment savedFile = fileService.saveAttachment(file);

        Discussion discussion = TestUtil.createValidDiscussion();
        discussion.setAttachment(savedFile);
        ResponseEntity<DiscussionVM> response = postDiscussion(discussion, DiscussionVM.class);

        long discussionId = response.getBody().getId();

        deleteDiscussion(discussionId, String.class);
        String attachmentFolderPath = appConfiguration.getFullAttachmentsPath() + "/" + savedFile.getName();
        File storedImage = new File(attachmentFolderPath);
        Assertions.assertFalse(storedImage.exists());
    }
}