package org.bot_docmng;


import java.io.File;
import java.io.InputStream;

class MessageFactory {
    private Task task;
    private String text;
    private String chatId;
    private InputStream sticker;
    private String stickerName;
    private TaskFile taskFile;
    private String replyId;

    MessageFactory(InputStream sticker, String stickerName, String chatId) {
        this.chatId = chatId;
        this.sticker = sticker;
        this.stickerName = stickerName;
    }

    MessageFactory(Task task) {
        this.task = task;
    }

    MessageFactory(TaskFile taskFile, String replyId, String chatId) {
        this.replyId = replyId;
        this.chatId = chatId;
        this.taskFile = taskFile;
    }

    MessageFactory(String text, String chatId) {
        this.text = text;
        this.chatId = chatId;
    }

    MessageFactory(String text, String chatId, String replyId) {
        this.text = text;
        this.chatId = chatId;
        this.replyId = replyId;
    }

    Message create() {
        if (task != null) {
            if (task.isHasFile()) {
                return new MessageFile(task);
            } else {
                return new MessageText(task);
            }
        } else if (text != null && chatId != null && replyId != null) {
            return new MessageSimple(text, chatId, replyId);
        } else if (text != null && chatId != null) {
            return new MessageSimple(text, chatId);
        } else if (sticker != null && stickerName != null && chatId != null) {
            return new MessageSticker(sticker, stickerName, chatId);
        } else if (taskFile != null && replyId != null && chatId != null) {
            return new MessageAdditionalFile(taskFile, replyId, chatId);
        } else {
            return null;
        }
    }
}
