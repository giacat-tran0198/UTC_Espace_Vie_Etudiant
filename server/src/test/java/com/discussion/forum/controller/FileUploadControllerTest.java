package com.discussion.forum.controller;

import com.discussion.forum.configuration.AppConfiguration;
import com.discussion.forum.domain.FileAttachment;
import com.discussion.forum.repository.FileAttachmentRepository;
import com.discussion.forum.repository.UserRepository;
import com.discussion.forum.service.UserService;
import com.discussion.forum.utils.TestUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.discussion.forum.utils.Constant.API_1_0_DISCUSSION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FileUploadControllerTest {
    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;


    public <T> ResponseEntity<T> uploadFile(HttpEntity<?> requestEntity, Class<T> responseType) {
        return testRestTemplate.exchange(API_1_0_DISCUSSION + "/upload", HttpMethod.POST, requestEntity, responseType);
    }

    private HttpEntity<MultiValueMap<String, Object>> getRequestEntity() {
        ClassPathResource imageResource = new ClassPathResource("profile.png");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", imageResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(body, headers);
    }

    private void authenticate(String username) {
        testRestTemplate.getRestTemplate()
                .getInterceptors().add(new BasicAuthenticationInterceptor(username, TestUtil.PASSWORD));
    }

    @BeforeEach
    public void cleanup() throws IOException {
        userRepository.deleteAll();
        fileAttachmentRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_receiveOk() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        ResponseEntity<Object> response = uploadFile(getRequestEntity(), Object.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void uploadFile_withImageFromUnauthorizedUser_receiveUnauthorized() {
        ResponseEntity<Object> response = uploadFile(getRequestEntity(), Object.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_receiveFileAttachmentWithDate() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        ResponseEntity<FileAttachment> response = uploadFile(getRequestEntity(), FileAttachment.class);
        Assertions.assertNotNull(Objects.requireNonNull(response.getBody()).getDate());
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_receiveFileAttachmentWithRandomName() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        ResponseEntity<FileAttachment> response = uploadFile(getRequestEntity(), FileAttachment.class);
        Assertions.assertNotNull(Objects.requireNonNull(response.getBody()).getName());
        Assertions.assertNotEquals("profile.png", response.getBody().getName());
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_imageSavedToFolder() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        ResponseEntity<FileAttachment> response = uploadFile(getRequestEntity(), FileAttachment.class);
        String imagePath = appConfiguration.getFullAttachmentsPath() + "/" + Objects.requireNonNull(response.getBody()).getName();
        File storedImage = new File(imagePath);
        Assertions.assertTrue(storedImage.exists());
    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_fileAttachmentSavedToDatabase() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        uploadFile(getRequestEntity(), FileAttachment.class);
        Assertions.assertEquals(1, fileAttachmentRepository.count());

    }

    @Test
    public void uploadFile_withImageFromAuthorizedUser_fileAttachmentStoredWithFileType() {
        userService.save(TestUtil.createValidUser("user1"));
        authenticate("user1");
        uploadFile(getRequestEntity(), FileAttachment.class);
        FileAttachment storedFile = fileAttachmentRepository.findAll().get(0);
        Assertions.assertEquals("image/png", storedFile.getFileType());

    }
}
