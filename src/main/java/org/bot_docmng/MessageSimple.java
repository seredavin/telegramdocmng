package org.bot_docmng;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MessageSimple implements Message {
    private String taskText;
    private String chatId;
    private String messageId;

    MessageSimple(String taskText, String chatId, String messageId) {
        this.taskText = taskText;
        this.chatId = chatId;
        this.messageId = messageId;
    }

    MessageSimple(String taskText, String chatId) {
        this.taskText = taskText;
        this.chatId = chatId;
    }

    @Override
    public void send() {
        Bot bot = new Bot(Setup.getInstance().getBotOptions());
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(taskText);
        message.setParseMode("Markdown");
        message.setReplyMarkup(bot.getStartKeyboard());
        try {
            bot.execute(message).getMessageId();
        } catch (TelegramApiException e) {
            BotLogger.error(e.getMessage());
        }
    }

    @Override
    public void update() {
        if (messageId != null) {
            Bot bot = new Bot(Setup.getInstance().getBotOptions());
            EditMessageText message = new EditMessageText();
            message.setChatId(chatId);
            message.setMessageId(Integer.parseInt(messageId));
            message.setText(taskText);
            message.setParseMode("Markdown");
            try {
                bot.execute(message);
            } catch (TelegramApiException e) {
                if (e.getMessage().equals("Error editing message text")) {
                    EditMessageCaption caption = new EditMessageCaption();
                    caption.setChatId(chatId);
                    caption.setMessageId(Integer.parseInt(messageId));
                    caption.setCaption(taskText);
                    caption.setParseMode("Markdown");
                    try {
                        bot.execute(caption);
                    } catch (TelegramApiException e1) {
                        BotLogger.error(e1.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void delete() {

    }
}
