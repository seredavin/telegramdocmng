package org.bot_docmng;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MessageAdditionalFile implements Message {
    private Bot bot;
    private TaskFile file;
    private String replyId;
    private String chatId;

    MessageAdditionalFile(TaskFile file, String replyId, String chatId) {
        this.file = file;
        this.replyId = replyId;
        this.chatId = chatId;
        this.bot = new Bot(Setup.getInstance().getBotOptions());
    }

    @Override
    public void send() {
        SendDocument document = new SendDocument();
        document.setChatId(chatId);
        document.setParseMode("Markdown");
        document.setReplyToMessageId(Integer.parseInt(replyId));
        org.telegram.telegrambots.meta.api.objects.Message message;
        if (file.getTelegramFileId().equals("")) {
            file.load();
            document.setDocument(file.getName(),
                    file.getFileInputStream());
            try {
                message = bot.execute(document);
                new Database().putFile(file.getUid(),
                        message.getDocument().getFileId());
                file.deleteFile();
            } catch (TelegramApiException e) {
                BotLogger.error(e.getMessage());
            }
        } else {
            document.setDocument(file.getTelegramFileId());
            try {
                bot.execute(document);
            } catch (TelegramApiException e) {
                BotLogger.error(e.getMessage());
            }
        }
    }

    @Override
    public void update() {

    }

    @Override
    public void delete() {

    }
}
