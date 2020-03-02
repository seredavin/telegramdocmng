package org.bot_docmng;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageFile implements Message {
    private Bot bot;
    private Task task;
    private org.telegram.telegrambots.meta.api.objects.Message message;

    MessageFile(Task task) {
        this.bot = new Bot(Setup.getInstance().getBotOptions());
        this.task = task;
    }

    @Override
    public void send() {
        SendDocument document = new SendDocument();
        document.setChatId(task.getPerformer().getChatID());
        document.setCaption(task.getMessageText());
        document.setParseMode("Markdown");
        document.setReplyMarkup(task.getKeyboardMarkup());
        if (task.getFirstFile().getTelegramFileId().equals("")) {
            task.getFirstFile().load();
            document.setDocument(task.getFirstFile().getName(),
                    task.getFirstFile().getFileInputStream());
            try {
                message = bot.execute(document);
                String fileId;
                try {
                    fileId = message.getDocument().getFileId();
                } catch (NullPointerException e) {
                    fileId = message.getAudio().getFileId();
                }
                new Database().putFile(task.getFirstFile().getUid(),
                        fileId);
                task.getFirstFile().deleteFile();
                task.putTask(Long.toString(message.getMessageId()));
            } catch (TelegramApiException e) {
                BotLogger.error(e.getMessage());
                task.getFirstFile().deleteFile();
            }
        } else {
            document.setDocument(task.getFirstFile().getTelegramFileId());
            try {
                message = bot.execute(document);
                task.putTask(Long.toString(message.getMessageId()));
            } catch (TelegramApiException e) {
                BotLogger.error(e.getMessage());
            }
        }

    }

    @Override
    public void update() {
        ArrayList<HashMap<String, String>> list = new Database().getTasksFromId(task.getUid());
        for (HashMap<String, String> map : list) {
            EditMessageCaption message = new EditMessageCaption();
            message.setChatId(map.get("chat_id"));
            message.setMessageId(Integer.parseInt(map.get("message_id")));
            message.setCaption(task.getMessageText());
            message.setParseMode("Markdown");
            message.setReplyMarkup(task.getKeyboardMarkup());
            try {
                bot.execute(message);
            } catch (TelegramApiException e) {
                BotLogger.error(e.getMessage());
            }
        }
    }

    @Override
    public void delete() {

    }
}
