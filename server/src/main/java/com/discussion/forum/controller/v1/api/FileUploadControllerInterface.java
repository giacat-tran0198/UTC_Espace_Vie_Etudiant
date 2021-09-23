package com.discussion.forum.controller.v1.api;

import com.discussion.forum.domain.FileAttachment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import static com.discussion.forum.utils.Constant.API_1_0_DISCUSSION;

@RequestMapping(API_1_0_DISCUSSION)
public interface FileUploadControllerInterface {
    @PostMapping("/upload")
    FileAttachment uploadForDiscussion(MultipartFile file);
}
