package org.bot_docmng;

import org.apache.logging.log4j.LogManager;

public class BotLogger {
    static final org.apache.logging.log4j.Logger rootLogger = LogManager.getRootLogger();

    static void info(String text) {
        rootLogger.info(text);
    }

    static void error(String text) {
        rootLogger.error(text);
    }
}
