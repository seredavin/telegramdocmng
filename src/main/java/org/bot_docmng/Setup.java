package org.bot_docmng;

import java.util.Map;

final class Setup {
    private static final Map<String, String> env = System.getenv();
    private static Setup instance;

    private Setup() {
    }

    static Setup getInstance() {
        if (instance == null) {
            instance = new Setup();
        }
        return instance;
    }

    String getBotUserName() {
        return env.get("TELEGRAM_USERNAME");
    }

    String getBotToken() {
        return env.get("TELEGRAM_TOKEN");
    }

    String getApiUser() {
        return env.get("API_USER");
    }

    String getApiPassword() {
        return env.get("API_PASSWORD");
    }

    String getApiURI() {
        return env.get("API_URI");
    }

    String getDbPath() {
        return env.get("DB_PATH");
    }
}
