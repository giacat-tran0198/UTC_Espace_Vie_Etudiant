package com.discussion.forum.service;

import com.discussion.forum.configuration.AppConfiguration;
import com.discussion.forum.domain.FileAttachment;
import com.discussion.forum.repository.FileAttachmentRepository;
import com.discussion.forum.service.impl.FileServiceImpl;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

@ActiveProfiles("test")
public class FileServiceTest {

    FileService fileService;

    AppConfiguration appConfiguration;

    @MockBean
    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @BeforeEach
    public void init() {
        appConfiguration = new AppConfiguration();
        appConfiguration.setUploadPath("uploads-test");

        fileService = new FileServiceImpl(appConfiguration, fileAttachmentRepository);

        new File(appConfiguration.getUploadPath()).mkdir();
        new File(appConfiguration.getFullProfileImagesPath()).mkdir();
        new File(appConfiguration.getFullAttachmentsPath()).mkdir();
    }

    @AfterEach
    public void cleanup() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }


    @Test
    public void detectType_whenPngFileProvided_returnsImagePng() throws IOException {
        ClassPathResource resourceFile = new ClassPathResource("test-png.png");
        byte[] fileArr = FileUtils.readFileToByteArray(resourceFile.getFile());
        String fileType = fileService.detectType(fileArr);
        Assertions.assertTrue(fileType.toLowerCase().contains("image/png"));
    }

//    @Test
//    public void cleanupStorage_whenOldFilesExist_removesFilesFromStorage() throws IOException {
//        String fileName = "random-file";
//        String filePath = appConfiguration.getFullAttachmentsPath() + "/" + fileName;
//        File source = new ClassPathResource("profile.png").getFile();
//        File target = new File(filePath);
//        FileUtils.copyFile(source, target);
//
//        FileAttachment fileAttachment = new FileAttachment();
//        fileAttachment.setId(5);
//        fileAttachment.setName(fileName);
//
//        Mockito.when(fileAttachmentRepository.findByDateBeforeAndDiscussionIsNull(Mockito.any(Date.class)))
//                .thenReturn(Arrays.asList(fileAttachment));
//
//        fileService.cleanupStorage();
//        File storedImage = new File(filePath);
//        Assertions.assertFalse(storedImage.exists());
//    }

//    @Test
//    public void cleanupStorage_whenOldFilesExist_remoevsFileAttachmentFromDatabase() throws IOException {
//        String fileName = "random-file";
//        String filePath = appConfiguration.getFullAttachmentsPath() + "/" + fileName;
//        File source = new ClassPathResource("profile.png").getFile();
//        File target = new File(filePath);
//        FileUtils.copyFile(source, target);
//
//        FileAttachment fileAttachment = new FileAttachment();
//        fileAttachment.setId(5);
//        fileAttachment.setName(fileName);
//
//        Mockito.when(fileAttachmentRepository.findByDateBeforeAndDiscussionIsNull(Mockito.any(Date.class)))
//                .thenReturn(Collections.singletonList(fileAttachment));
//
//        fileService.cleanupStorage();
//        Mockito.verify(fileAttachmentRepository).deleteById(5L);
//    }

}
