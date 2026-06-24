package com.revy.talkblogback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private Long messageId;
    private Long groupId;
    private Long senderId;
    private Short messageType;
    private String content;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private Short isDeleted;
    private Short isRecalled;
    private LocalDateTime createdAt;

    public static final short TYPE_TEXT = 1;
    public static final short TYPE_IMAGE = 2;
    public static final short TYPE_FILE = 3;

    public static final short DELETED_NO = 0;
    public static final short DELETED_YES = 1;

    public static final short RECALLED_NO = 0;
    public static final short RECALLED_YES = 1;
}
