package org.bot_docmng;

import com.google.gson.Gson;
import org.aopalliance.reflect.Class;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class Task {
    private Performer performer;
    private String name;
    private String type;
    private Performer author;
    private String description;
    private String result_comment;
    private String uid;
    private String status;
    private TaskFile[] taskFiles;
    private boolean hasFile;
    private TaskFile firstFile;


    Task(String chatId, String messageId) {
        this(new Database().getTaskUidFromMessage(messageId, chatId));
    }

    private Task(String uid) {
        String json = null;
        try {
            json = Connector.getRequest(null, "task/" + uid);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            BotLogger.error(e.getMessage());
        }
        Gson gson = new Gson();
        JsonTask jsonTask = new JsonTask();
        try {
            jsonTask = gson.fromJson(json, JsonTask.class);
        } catch (com.google.gson.JsonSyntaxException e) {
            BotLogger.error("task uid: " + uid
                    + ", josn: " + json
                    + ", Exception: " + e.getMessage());
        }
        this.performer = new Performer(jsonTask.performer_uid);
        this.name = jsonTask.task_name;
        this.type = (jsonTask.task_type + " ").split(" ")[0];
        this.author = new Performer(jsonTask.author_uid);
        this.description = jsonTask.task_description;
        this.result_comment = jsonTask.task_result_comment;
        this.uid = jsonTask.task_uid;
        this.status = jsonTask.done;
        if (jsonTask.files.length > 0) {
            TaskFile[] taskFiles = new TaskFile[jsonTask.files.length];
            for (int i = 0; i < jsonTask.files.length; i++) {
                taskFiles[i] = new TaskFile(jsonTask.files[i].file_uid);
            }
            this.taskFiles = taskFiles;
            this.hasFile = true;
            this.firstFile = taskFiles[0];
        }
    }

    static void sendUnDoneTask(long chatId, Integer messageId) {
        Performer performer = new Performer(Long.toString(chatId));
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("img/wait.webp");
        new MessageFactory(is, "wait", Long.toString(chatId)).create().send();
//        new MessageFactory(new File("img/", "wait.webp"), Long.toString(chatId)).create().send();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        ArrayList<String> tasksUid = Task.getTasksBuPerformer(performer, "", dateFormat.format(new Date()));
        new MessageFactory("*Незавершённых задач: " + tasksUid.size() + "*",
                performer.getChatID()).create().send();
        for (String taskUid : tasksUid) {
            Task task = new Task(taskUid);
            task.setPerformer(new Performer(Long.toString(chatId)));
            new MessageFactory(task).create().send();
            BotLogger.info("Chat id: " + chatId
                    + ", Message id: " + messageId
                    + ", Task id: " + taskUid);
        }
        new MessageFactory("*Конец!*", Long.toString(chatId)).create().send();
    }

    static void sendTasksFromPeriod() {
        BotLogger.info("Start sending task from period");
        Performer[] performers = Performer.getPerformers();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String begin = new Database().getTimeByFunction("scheduler_timestamp");
        String end = dateFormat.format(new Date());
        BotLogger.info("Start time: " + begin + ", End time: " + end);
        for (int i = 0; i < performers.length; i++) {
            ArrayList<String> tasksUids = Task.getTasksBuPerformer(performers[i], begin, end);
            for (String taskUid : tasksUids) {
                Task task = new Task(taskUid);
                task.setPerformer(new Performer(performers[i].getUid()));
                new MessageFactory(task).create().send();
                BotLogger.info("Chat id: " + performers[i].getChatID()
                        + ", Task id: " + taskUid);
            }
        }
        BotLogger.info("End sending task from period");
        new Database().putTimeByFunction("scheduler_timestamp", end);
    }

    private static void sendTasks(Task[] tasks) {
        for (Task task : tasks) {
            Message message = new MessageFactory(task).create();
            message.send();
        }
    }

    private static ArrayList<String> getTasksBuPerformer(Performer performer, String begin, String end) {
        Map<String, String> params = new HashMap<>();
        if (!begin.equals("")) {
            params.put("begin", begin);
        }
        params.put("end", end);
        String json = null;
        try {
            json = Connector.getRequest(params, "tasks/" + performer.getUid());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            BotLogger.error(e.getMessage());
        }
        Gson gson = new Gson();
        JsonTasks[] jsonTasks = gson.fromJson(json, JsonTasks[].class);
        ArrayList<String> tasksUid = new ArrayList<>();
        for (int i = 0; i < jsonTasks.length; i++) {
            tasksUid.add(jsonTasks[i].task_id);
        }
        return tasksUid;
    }

    String getUid() {
        return uid;
    }

    Performer getPerformer() {
        return performer;
    }

    void setPerformer(Performer performer) {
        this.performer = performer;
    }

    TaskFile getFirstFile() {
        return firstFile;
    }

    boolean isHasFile() {
        return hasFile;
    }

    TaskFile[] getFileArray() {
        if (hasFile && taskFiles.length > 1) {
            return taskFiles;
        } else {
            return new TaskFile[0];
        }
    }

    String getMessageText() {
        String text = "*" + name + "*";
        if (!description.equals("")) {
            text += "\n\n" + description;
        }
        text += "\n\nАвтор: " + author.getName();
        int limit = 1024;
        return (text.length() > limit ? text.substring(0, limit) : text);
    }

    InlineKeyboardMarkup getKeyboardMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        if (status.equals("no")) {
            switch (this.type) {
                case "Согласовать":
                    rows.add(new ArrayList<>(Arrays.asList(
                            new InlineKeyboardButton().setText("✅ Согласовано").setCallbackData("yes"),
                            new InlineKeyboardButton().setText("❌ Не согласовано").setCallbackData("no")
                    )));
                    break;
                case "Ознакомиться":
                    rows.add(new ArrayList<>(Collections.singletonList(
                            new InlineKeyboardButton().setText("✅ Ознакомлен").setCallbackData("yes")
                    )));
                    break;
                case "Проверить":
                    rows.add(new ArrayList<>(Collections.singletonList(
                            new InlineKeyboardButton().setText("✅ Проверено").setCallbackData("yes")
                    )));
                    break;
                case "Исполнить":
                    rows.add(new ArrayList<>(Collections.singletonList(
                            new InlineKeyboardButton().setText("✅ Исполнено").setCallbackData("yes")
                    )));
                    break;
                case "Рассмотреть":
                    rows.add(new ArrayList<>(Collections.singletonList(
                            new InlineKeyboardButton().setText("✅ Рассмотрено").setCallbackData("yes")
                    )));
                    break;
                case "Утвердить":
                    rows.add(new ArrayList<>(Arrays.asList(
                            new InlineKeyboardButton().setText("✅ Утверждено").setCallbackData("yes"),
                            new InlineKeyboardButton().setText("❌ Не утверждено").setCallbackData("no")
                    )));
                    break;
            }
        }
        if (hasFile && taskFiles.length > 1) {
            rows.add(new ArrayList<>(Collections.singletonList(
                    new InlineKeyboardButton().setText("\uD83D\uDCD1️ Прислать дополнительные файлы")
                            .setCallbackData("addition")
            )));
        }
        rows.add(new ArrayList<>(Arrays.asList(
                new InlineKeyboardButton().setText("➡️ Делегировать").setCallbackData("delegate"),
                new InlineKeyboardButton().setText("\uD83D\uDD01 Обновить").setCallbackData("update")
        )));
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getDelegateKeyboardMarkup() {
        return null;
    }

    void putTask(String messageId) {
        Database database = new Database();
        HashMap<String, String> map = new HashMap<>();
        map.put("task_uid", getUid());
        map.put("performer_uid", getPerformer().getUid());
        map.put("message_id", messageId);
        map.put("chat_id", getPerformer().getChatID());
        database.putTask(map);
    }

    String resolveTask(boolean reply) {
        Map<String, String> params = new HashMap<>();
        params.put("resolve", (reply != false ? "yes" : "no"));
        params.put("performer_uid", performer.getUid());
        params.put("comment", "Выполнено через telegram");
        String json = null;
        try {
            json = Connector.postRequest(params, "task/" + uid);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            BotLogger.error(e.getMessage());
        }
        Gson gson = new Gson();
        JsonResolveResponse jsonResolveResponse = gson.fromJson(json, JsonResolveResponse.class);
        if (jsonResolveResponse.done.equals("yes")) {
            return "Успешно";
        } else {
            return "Ошибка";
        }
    }
}