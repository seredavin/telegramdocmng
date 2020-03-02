package org.bot_docmng;

import com.google.gson.Gson;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class Performer {
    private String name;
    private String uid;
    private String chatID;


    Performer(String uid) {
        HashMap<String, String> map = new Database().getPerformer(uid);
        if (map.size() > 0) {
            this.uid = map.get("user_id");
            this.name = map.get("name");
            this.chatID = map.get("chat_id");
        } else {
            String json = null;
            try {
                json = Connector.getRequest(new HashMap<>(), "user/" + uid);
            } catch (URISyntaxException | IOException | InterruptedException e) {
                BotLogger.error(e.getMessage());
            }
            Gson gson = new Gson();
            JsonPerformer jsonPerformer = gson.fromJson(json, JsonPerformer.class);
            this.name = jsonPerformer.name;
            this.chatID = jsonPerformer.chat_id;
            this.uid = jsonPerformer.user_id;
            HashMap<String, String> outMap = new HashMap<>();
            outMap.put("name", this.name);
            outMap.put("chat_id", this.chatID);
            outMap.put("user_id", this.uid);
            new Database().putPerformer(outMap);
        }
    }

    static Performer[] getPerformers() {
        String json = null;
        try {
            json = Connector.getRequest(null, "performers");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            BotLogger.error(e.getMessage());
        }
        Gson gson = new Gson();
        JsonPerformers[] jsonPerformers = gson.fromJson(json, JsonPerformers[].class);
        Performer[] performers = new Performer[jsonPerformers.length];

        for (int i = 0; i < jsonPerformers.length; i++) {
            performers[i] = new Performer(jsonPerformers[i].performer_uid);
        }

        return performers;
    }

    String getName() {
        return name;
    }

    String getUid() {
        return uid;
    }

    String getChatID() {
        return chatID;
    }

    @Override
    public String toString() {
        return name;
    }

    void getSubUsersKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        Performer[] subUsers = getSubUsers();
        for (Performer user : subUsers) {
            rows.add(new ArrayList<>(Collections.singletonList(
                    new InlineKeyboardButton().setText(user.getName()).setCallbackData("sub_" + user.getUid())
            )));
        }
//        rows.add(new ArrayList<>(Arrays.asList(
//                new InlineKeyboardButton().setText("⬅️ Назад").setCallbackData("sub_back")
//        )));
        keyboardMarkup.setKeyboard(rows);
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText("Кому?");
        message.setParseMode("Markdown");
        message.setReplyMarkup(keyboardMarkup);

        Bot bot = new Bot(Setup.getInstance().getBotOptions());
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            BotLogger.error(e.getMessage());
        }
    }

    private Performer[] getSubUsers() {
        String json = null;
        try {
            json = Connector.getRequest(null, "subusers/" + uid);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            BotLogger.error(e.getMessage());
        }
        Gson gson = new Gson();
        JsonSubUsers[] jsonSubusers = gson.fromJson(json, JsonSubUsers[].class);
        Performer[] performers = new Performer[jsonSubusers.length];
        for (int i = 0; i < jsonSubusers.length; i++) {
            performers[i] = new Performer(jsonSubusers[i].user_id);
        }
        return performers;
    }
}