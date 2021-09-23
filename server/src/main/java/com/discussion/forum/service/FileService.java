package com.discussion.forum.service;

import com.discussion.forum.domain.FileAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    String saveProfileImage(String base64Image) throws IOException;
    String detectType(byte[] fileArr);
    void deleteProfileImage(String image);
    FileAttachment saveAttachment(MultipartFile file);
    void cleanupStorage();
    void deleteAttachmentImage(String image);
}
