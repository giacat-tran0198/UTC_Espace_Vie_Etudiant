package com.discussion.forum.repository;

import com.discussion.forum.domain.Discussion;
import com.discussion.forum.domain.FileAttachment;
import com.discussion.forum.utils.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
public class FileAttachmentRepositoryTest {
    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;
    
    private FileAttachment getOneHourOldFileAttachment() {
        Date date = new Date(System.currentTimeMillis() - (60*60*1000) - 1);
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }
    private FileAttachment getFileAttachmentWithinOneHour() {
        Date date = new Date(System.currentTimeMillis() - (60*1000));
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }

    private FileAttachment getOldFileAttachmentWithDiscussion(Discussion discussion) {
        FileAttachment fileAttachment = getOneHourOldFileAttachment();
        fileAttachment.setDiscussion(discussion);
        return fileAttachment;
    }

    @Test
    public void findByDateBeforeAndDiscussionIsNull_whenAttachmentsDateOlderThanOneHour_returnsAll() {
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndDiscussionIsNull(oneHourAgo);
        Assertions.assertEquals(3, attachments.size());
    }

    @Test
    public void findByDateBeforeAndDiscussionIsNull_whenAttachmentsDateOlderThanOneHorButHaveDiscussion_returnsNone() {
        Discussion discussion1 = testEntityManager.persist(TestUtil.createValidDiscussion());
        Discussion discussion2 = testEntityManager.persist(TestUtil.createValidDiscussion());
        Discussion discussion3 = testEntityManager.persist(TestUtil.createValidDiscussion());

        testEntityManager.persist(getOldFileAttachmentWithDiscussion(discussion1));
        testEntityManager.persist(getOldFileAttachmentWithDiscussion(discussion2));
        testEntityManager.persist(getOldFileAttachmentWithDiscussion(discussion3));
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndDiscussionIsNull(oneHourAgo);
        Assertions.assertEquals(0, attachments.size());
    }

    @Test
    public void findByDateBeforeAndDiscussionIsNull_whenAttachmentsDateWithinOneHour_returnsNone() {
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndDiscussionIsNull(oneHourAgo);
        Assertions.assertEquals(0, attachments.size());
    }

    @Test
    public void findByDateBeforeAndDiscussionIsNull_whenSomeAttachmentsOldSomeNewAndSomeWithDiscussion_returnsAttachmentsWithOlderAndNoDiscussionAssigned() {
        Discussion discussion1 = testEntityManager.persist(TestUtil.createValidDiscussion());
        testEntityManager.persist(getOldFileAttachmentWithDiscussion(discussion1));
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndDiscussionIsNull(oneHourAgo);
        Assertions.assertEquals(1, attachments.size());
    }
}
