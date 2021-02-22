package io.github.anisthesie.db;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.requests.direct.DirectThreadsBroadcastRequest;
import com.github.instagram4j.instagram4j.requests.media.MediaCommentRequest;
import io.github.anisthesie.bot.InstagramBot;
import io.github.anisthesie.bot.actions.follow.FollowToken;
import io.github.anisthesie.bot.actions.scheduling.ScheduledPost;
import io.github.anisthesie.bot.jobs.DefaultUserJob;
import lombok.Getter;
import lombok.Setter;
import org.quartz.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class User {

    @Getter
    @Setter
    private String username, password;

    @Getter
    @Setter
    private boolean following, commenting, messaging;

    @Getter
    private final List<FollowToken> locationTokenList = new ArrayList<>();

    @Getter
    private final List<FollowToken> accountTokenList = new ArrayList<>();

    @Getter
    private final List<FollowToken> hashtagTokenList = new ArrayList<>();

    @Getter
    private IGClient client;

    @Getter
    private int failure = 0;

    @Getter
    @Setter
    private String customMessage;

    @Getter @Setter
    private ScheduledPost scheduledPost;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, boolean following, boolean commenting, boolean messaging, String customMessage) {
        this.username = username;
        this.password = password;
        this.following = following;
        this.commenting = commenting;
        this.customMessage = customMessage;
        this.messaging = messaging;
    }

    public void initJob() {

        final JobKey jobKey = JobKey.jobKey(username);
        final JobDetail jobDetail = JobBuilder.newJob(DefaultUserJob.class).withIdentity(jobKey).build();
        final Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(username)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(15)
                        .repeatForever())
                .build();

        try {
            InstagramBot.scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public boolean hasScheduledPost() {
        return scheduledPost != null;
    }

    public boolean login() {
        try {
            client = IGClient.builder()
                    .username(username)
                    .password(password)
                    .login();
        } catch (IGLoginException e) {
            e.printStackTrace();
            return false;
        }
        return isLogged();
    }

    public void postComment(String itemId, String text) {
        try {
            new MediaCommentRequest(itemId, text).execute(this.client).get(20, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String text, String thread_id) throws InterruptedException, ExecutionException, TimeoutException {
        new DirectThreadsBroadcastRequest(
                new DirectThreadsBroadcastRequest.BroadcastTextPayload(text, thread_id)).execute(client)
                .get(20, TimeUnit.SECONDS);

    }

    public boolean isLogged() {
        return client != null && client.isLoggedIn();
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User)
            return ((User) obj).getUsername().equalsIgnoreCase(username);
        return false;
    }

    @Override
    public String toString() {
        return username;
    }

    public void incrementFailure() {
        failure++;
    }
}
