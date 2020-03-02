package org.bot_docmng;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bot extends TelegramLongPollingBot {
    private static String botUserName = Setup.getInstance().getBotUserName();
    private static String token = Setup.getInstance().getBotToken();

    Bot(DefaultBotOptions botOptions) {
        super(botOptions);
        botUserName = Setup.getInstance().getBotUserName();
        token = Setup.getInstance().getBotToken();
    }

    static void start() {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            Bot mybot = new Bot(Setup.getInstance().getBotOptions());
            telegramBotsApi.registerBot(mybot);
        } catch (TelegramApiRequestException e) {
            BotLogger.error(e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                BotLogger.info("Chat id: " + update.getMessage().getChatId()
                        + ", Message id: " + update.getMessage().getMessageId()
                        + ", Text: " + update.getMessage().getText());
                switch (update.getMessage().getText()) {
                    case "/start":
                        try {
                            execute(sendStartMessage(update.getMessage().getChatId()));
                        } catch (TelegramApiException e) {
                            BotLogger.error("Chat id: " + update.getMessage().getChatId()
                                    + ", Message id: " + update.getMessage().getMessageId()
                                    + ", Exception: " + e.getMessage());
                        }
                        break;
                    case "/btn":
                        try {
                            execute(new SendMessage()
                                    .setChatId(update.getMessage().getChatId())
                                    .setText("⌨")
                                    .setReplyMarkup(getStartKeyboard()));
                        } catch (TelegramApiException e) {
                            BotLogger.error("Chat id: " + update.getMessage().getChatId()
                                    + ", Message id: " + update.getMessage().getMessageId()
                                    + ", Exception: " + e.getMessage());
                        }
                        break;
                    case "Поставить новую задачу ➕":
                        Runnable RunNewTask = () -> new Performer(Long.toString(update.getMessage().getChatId())).getSubUsersKeyboard();
                        new Thread(RunNewTask).start();
                        break;
                    case "Получить все мои незавершенные задачи":
                        Runnable runUnDoneTask = () -> Task.sendUnDoneTask(update.getMessage().getChatId(), update.getMessage().getMessageId());
                        new Thread(runUnDoneTask).start();
                        break;
                }
                if (update.getMessage().isReply()) {
                    Performer author = new Performer(Long.toString(update
                            .getMessage().getChatId()));
                    String performerUid = new Database().getAddTask(author.getUid(),
                            Long.toString(update.getMessage().getReplyToMessage().getMessageId()));
                    Performer performer = new Performer(performerUid);
                    if (!performerUid.equals("")) {
                        new AddTask(author, performer, update.getMessage().getText()).sendTask();
                    }
                }

            }
        } else if (update.hasCallbackQuery()) {
            BotLogger.info("Chat id: " + update.getCallbackQuery().getMessage().getChatId()
                    + ", Message id: " + update.getCallbackQuery().getMessage().getMessageId()
                    + ", CallbackQuery: " + update.getCallbackQuery().getData());
            if (update.getCallbackQuery().getData().contains("sub_")) {
                Runnable runSub = () -> {
                    Performer subUser = new Performer(update.getCallbackQuery().getData().split("_", 2)[1]);
                    Performer author = new Performer(Long.toString(update.getCallbackQuery().getMessage().getChatId()));
                    try {
                        HashMap<String, String> map = new HashMap<>();
                        String messageId = Long.toString(execute(new SendMessage().setText("Введите новую задачу для " + subUser)
                                .setChatId(update.getCallbackQuery().getMessage().getChatId())
                                .setReplyMarkup(new ForceReplyKeyboard())).getMessageId());
                        map.put("author_uid", author.getUid());
                        map.put("performer_uid", subUser.getUid());
                        map.put("message_id", messageId);
                        new Database().putAddTask(map);
                    } catch (TelegramApiException e) {
                        BotLogger.info("Chat id: " + update.getCallbackQuery().getMessage().getChatId()
                                + ", Message id: " + update.getCallbackQuery().getMessage().getMessageId()
                                + ", CallbackQuery: " + update.getCallbackQuery().getData()
                                + ", Exception: " + e.getMessage());
                    }
                };
                new Thread(runSub).start();
            } else if (update.getCallbackQuery().getData().contains("update")) {
                Runnable runUpdate = () -> new MessageFactory(new Task(Long.toString(update.getCallbackQuery().getMessage().getChatId()),
                        Long.toString(update.getCallbackQuery().getMessage().getMessageId()))).create().update();
                new Thread(runUpdate).start();
            } else if (update.getCallbackQuery().getData().contains("addition")) {
                Runnable runAddition = () -> {
                    Task task = new Task(Long.toString(update.getCallbackQuery().getMessage().getChatId()),
                            Long.toString(update.getCallbackQuery().getMessage().getMessageId()));
                    TaskFile[] fileArray = task.getFileArray();

                    if (task.isHasFile() && fileArray.length > 1) {
                        for (int i = 1; i < fileArray.length; i++) {
                            new MessageFactory(fileArray[i],
                                    Long.toString(update.getCallbackQuery().getMessage().getMessageId()),
                                    Long.toString(update.getCallbackQuery().getMessage().getChatId())).create().send();
                        }
                    }
                };
                new Thread(runAddition).start();
            } else if (update.getCallbackQuery().getData().contains("yes") ||
                    update.getCallbackQuery().getData().contains("no")) {
                Runnable runResponse = () -> {
                    boolean request = update.getCallbackQuery().getData().contains("yes");
                    Task task = new Task(Long.toString(update.getCallbackQuery().getMessage().getChatId()),
                            Long.toString(update.getCallbackQuery().getMessage().getMessageId()));
                    task.setPerformer(new Performer(Long.toString(update.getCallbackQuery().getMessage().getChatId())));
                    new MessageFactory("*Задача выполняется* \n *Немного подождите*",
                            Long.toString(update.getCallbackQuery().getMessage().getChatId()),
                            Long.toString(update.getCallbackQuery().getMessage().getMessageId())).create().update();
                    String response = task.resolveTask(request);
                    BotLogger.info("Chat id: " + update.getCallbackQuery().getMessage().getChatId()
                            + ", Message id: " + update.getCallbackQuery().getMessage().getMessageId()
                            + ", CallbackQuery: " + update.getCallbackQuery().getData()
                            + ", Response: " + response);
                    try {
                        Thread.sleep(15000);
                        new MessageFactory(new Task(Long.toString(update.getCallbackQuery().getMessage().getChatId()),
                                Long.toString(update.getCallbackQuery().getMessage().getMessageId()))).create().update();
                    } catch (InterruptedException e) {
                        BotLogger.info("Chat id: " + update.getCallbackQuery().getMessage().getChatId()
                                + ", Message id: " + update.getCallbackQuery().getMessage().getMessageId()
                                + ", CallbackQuery: " + update.getCallbackQuery().getData()
                                + ", Exception: " + e.getMessage());
                    }
                };
                new Thread(runResponse).start();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    private SendMessage sendStartMessage(long chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getStartKeyboard();
        return new SendMessage()
                .setChatId(chatId)
                .setText("Передайте этот id администратору \n" + chatId)
                .setReplyMarkup(replyKeyboardMarkup)
                .enableMarkdown(true);
    }

    ReplyKeyboardMarkup getStartKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow1.add(new KeyboardButton("Получить все мои незавершенные задачи"));
        keyboardRow2.add(new KeyboardButton("Поставить новую задачу ➕"));
        List<KeyboardRow> rowList1 = new ArrayList<>();
        rowList1.add(keyboardRow1);
        rowList1.add(keyboardRow2);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup.setKeyboard(rowList1);
    }
}