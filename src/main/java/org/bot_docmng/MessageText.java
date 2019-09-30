package org.bot_docmng;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;


public class MessageText implements Message {
    private Bot bot;
    private Task task;

    MessageText(Task task) {
        this.bot = new Bot();
        this.task = task;
    }

    @Override
    public void send() {
        SendMessage message = new SendMessage();
        message.setChatId(task.getPerformer().getChatID());
        message.setText(task.getMessageText());
        message.setParseMode("Markdown");
        message.setReplyMarkup(task.getKeyboardMarkup());
        try {
            String messageId = Long.toString(bot.execute(message).getMessageId());
            HashMap<String, String> map = new HashMap<>();
            map.put("task_uid", task.getUid());
            map.put("performer_uid", task.getPerformer().getUid());
            map.put("message_id", messageId);
            map.put("chat_id", task.getPerformer().getChatID());
            new Database().putTask(map);
        } catch (TelegramApiException e) {
            BotLogger.error(e.getMessage());
        }
    }

    @Override
    public void update() {
        ArrayList<HashMap<String, String>> list = new Database().getTasksFromId(task.getUid());
        for (HashMap<String, String> map : list) {
            EditMessageText message = new EditMessageText();
            message.setChatId(map.get("chat_id"));
            message.setMessageId(Integer.parseInt(map.get("message_id")));
            message.setText(task.getMessageText());
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
