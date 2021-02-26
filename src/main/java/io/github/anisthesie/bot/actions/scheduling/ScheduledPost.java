package io.github.anisthesie.bot.actions.scheduling;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

public class ScheduledPost {

    @Getter @Setter
    private TimeUnit timeUnit;

    @Getter @Setter
    private long delay;

    @Getter @Setter
    private String URL;

    public ScheduledPost(TimeUnit timeUnit, long delay, String URL) {
        this.timeUnit = timeUnit;
        this.delay = delay;
        this.URL = URL;
    }
}
