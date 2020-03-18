package org.bot_docmng;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;

import java.util.Map;

final class Setup {
    private static final Map<String, String> env = System.getenv();
    private static Setup instance;

    private static DefaultBotOptions botOptions;

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

    DefaultBotOptions getBotOptions() {
        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
        if (env.get("SOCKS_HOST") != null && env.get("SOCKS_PORT") != null) {
            botOptions.setProxyHost(env.get("SOCKS_HOST"));
            botOptions.setProxyPort(Integer.parseInt(env.get("SOCKS_PORT")));
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
        }
        return botOptions;
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
