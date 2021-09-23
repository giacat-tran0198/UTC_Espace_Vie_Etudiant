package com.discussion.forum.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Data
public class Discussion {
    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Size(min = 10, max = 5000)
    @Column(length = 5000)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @ManyToOne
    private User user;

    @OneToOne(mappedBy="discussion", orphanRemoval = true)
    private FileAttachment attachment;
}
