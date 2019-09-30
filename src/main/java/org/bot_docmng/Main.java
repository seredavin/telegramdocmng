package org.bot_docmng;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        Runnable RunBot = () -> Bot.start();
        new Thread(RunBot).start();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(() -> Task.sendTasksFromPeriod(), 1, 1, TimeUnit.MINUTES);
    }
}
