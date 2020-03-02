package org.bot_docmng;

import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;

public class MessageSticker implements Message {
    private String chatId;
    private String stickerName;
    private InputStream sticker;

    MessageSticker(InputStream sticker, String stickerName, String chatId) {
        this.chatId = chatId;
        this.sticker = sticker;
        this.stickerName = stickerName;
    }

    @Override
    public void send() {
        Bot bot = new Bot(Setup.getInstance().getBotOptions());
        SendSticker message = new SendSticker();
        message.setChatId(chatId);
        message.setSticker(stickerName, sticker);
        try {
            bot.execute(message).getMessageId();
        } catch (TelegramApiException e) {
            BotLogger.error(e.getMessage());
        }
    }

    @Override
    public void update() {

    }

    @Override
    public void delete() {

    }
}
