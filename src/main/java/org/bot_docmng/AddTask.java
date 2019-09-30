package org.bot_docmng;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

class AddTask {
    private Performer author;
    private Performer performer;
    private String taskText;

    AddTask(Performer author, Performer performer, String taskText) {
        this.author = author;
        this.performer = performer;
        this.taskText = taskText;
    }

    void sendTask() {
        HashMap<String, String> map = new HashMap<>();
        map.put("author_uid", author.getUid());
        map.put("performer_uid", performer.getUid());
        map.put("task_name", taskText);
        try {
            String response = Connector.postRequest(map, "add_task");
            if (response.equals("Success")) {
                new MessageFactory("Новая задача для " + performer + " добавлена.",
                        author.getChatID()).create().send();
            } else {
                new MessageFactory("Ошибка: " + response, author.getChatID()).create().send();
            }
            BotLogger.info("Add new task: " + response);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            BotLogger.error(e.getMessage());
        }
    }
}
