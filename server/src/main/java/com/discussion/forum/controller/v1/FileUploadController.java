package com.discussion.forum.controller.v1;

import com.discussion.forum.controller.v1.api.FileUploadControllerInterface;
import com.discussion.forum.domain.FileAttachment;
import com.discussion.forum.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

@RestController
public class FileUploadController implements FileUploadControllerInterface {

    @Autowired
    FileService fileService;

    @Override
    public FileAttachment uploadForDiscussion(MultipartFile file) {
        return fileService.saveAttachment(file);
    }
}
