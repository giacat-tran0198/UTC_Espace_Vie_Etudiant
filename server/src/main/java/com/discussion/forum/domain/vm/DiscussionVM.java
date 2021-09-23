package com.discussion.forum.domain.vm;

import com.discussion.forum.domain.Discussion;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DiscussionVM {
    private long id;

    private String content;

    private long date;

    private UserVM user;

    private FileAttachmentVM attachment;

    public DiscussionVM(Discussion discussion) {
        this.setId(discussion.getId());
        this.setContent(discussion.getContent());
        this.setDate(discussion.getTimestamp().getTime());
        this.setUser(new UserVM(discussion.getUser()));
        if(discussion.getAttachment() != null) {
            this.setAttachment(new FileAttachmentVM(discussion.getAttachment()));
        }
    }
}
