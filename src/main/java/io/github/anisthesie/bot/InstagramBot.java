package io.github.anisthesie.bot;

import com.github.instagram4j.instagram4j.actions.users.UserAction;
import com.github.instagram4j.instagram4j.requests.direct.DirectGetByParticipantsRequest;
import com.github.instagram4j.instagram4j.responses.direct.DirectThreadsResponse;
import io.github.anisthesie.bot.actions.follow.FollowToken;
import io.github.anisthesie.bot.actions.follow.FollowType;
import io.github.anisthesie.bot.actions.scheduling.ScheduledPost;
import io.github.anisthesie.bot.jobs.DefaultUserJob;
import io.github.anisthesie.bot.jobs.SchedulePostJob;
import io.github.anisthesie.db.Database;
import io.github.anisthesie.db.User;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class InstagramBot {

    public static final HashMap<String, User> USERS = new HashMap<>();

    public static Scheduler scheduler;

    public static Random random = new Random();

    public static final String[] emojis = {"\uD83E\uDD23", // Laughing on the floor
            "\uD83D\uDC7D", // Alien emoji
            "\uD83E\uDD75", // Hot face
            "\u2764\uFE0F", // Red heart
            "\uD83D\uDE02", // Laughing
            "\uD83D\uDD25", // Fire
    };

    public static void startBot() {
        if (!Database.isConnected())
            return;
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            initUsers();
        } catch (SchedulerException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initUsers() throws SQLException, SchedulerException {
        if (!Database.isConnected()) {
            System.out.println("Database unavailable, aborting.");
            return;
        }
        final Statement statement = Database.connection.createStatement();
        final ResultSet resultSet = statement.executeQuery("SELECT username, password, following, messaging, commenting, tokens, custom_message from " + Database.USERS_TABLE);
        processSQL(resultSet);
    }

    public static String sendDm(String username, String target, String message) {
        if (InstagramBot.USERS.containsKey(username)) try {
            final User user = InstagramBot.USERS.get(username);
            if(!user.isLogged())
                user.login();
            final UserAction userAction = user.getClient().actions().users().findByUsername(target).join();
            if (userAction == null || userAction.getUser() == null)
                return "Error. Target user not found";
            DirectThreadsResponse directThreadsResponse =
                    new DirectGetByParticipantsRequest(userAction.getUser().getPk()).execute(user.getClient()).get(20, TimeUnit.SECONDS);
            if (directThreadsResponse.getThread() != null)
                user.sendMessage(message, directThreadsResponse.getThread().getThread_id());
            return "Success.";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "Error. User not in database or not updated.";
    }

    public static String schedulePost(String username, int delay, String url) {
        if (InstagramBot.USERS.containsKey(username)) {
            final User user = InstagramBot.USERS.get(username);
            if (user.hasScheduledPost())
                return "Error. User already has a scheduled post.";
            user.setScheduledPost(new ScheduledPost(TimeUnit.MINUTES, delay, url));
            final JobKey jobKey = JobKey.jobKey("sj:" + username);
            final JobDetail jobDetail = JobBuilder.newJob(SchedulePostJob.class).withIdentity(jobKey).build();
            final Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobKey.getName())
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMinutes(delay)
                            .withRepeatCount(1))
                    .build();

            try {
                InstagramBot.scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException e) {
                e.printStackTrace();
                return e.getMessage();
            }
            return "Success.";
        }
        return "Error. User not in database or not updated.";
    }

    public static String updateUser(String query) {

        if (!Database.isConnected()) {
            return "Database unavailable.";
        }
        InstagramBot.USERS.remove(query);
        try {
            final Statement statement = Database.connection.createStatement();
            final ResultSet resultSet = statement.executeQuery(
                    "SELECT username, password, following, messaging, commenting, tokens, custom_message from " + Database.USERS_TABLE + " WHERE username = " + query);
            processSQL(resultSet);
            return "Success. Updated user.";
        } catch (SQLException | SchedulerException throwables) {
            throwables.printStackTrace();
            return throwables.getMessage();
        }
    }

    private static void processSQL(ResultSet resultSet) throws SQLException, SchedulerException {
        while (resultSet.next()) {
            final String username = resultSet.getString("username");
            final String password = resultSet.getString("password");
            final boolean following = resultSet.getInt("following") == 1;
            final boolean commenting = resultSet.getInt("commenting") == 1;
            final boolean messaging = resultSet.getInt("messaging") == 1;
            final String tokens = resultSet.getString("tokens");
            final String customMessage = resultSet.getString("custom_message");
            final User user = new User(username, password, following, commenting, messaging, customMessage);
            if (!USERS.containsKey(username))
                USERS.put(username, user);
            if (following || commenting) {
                final String[] tokenArray = tokens.split(",");
                for (final String token : tokenArray) {
                    final String[] split = token.split(":");
                    if (split.length != 2) continue;
                    final FollowType type = FollowType.fromString(split[0]);
                    if (type == null) continue;
                    final FollowToken followToken = new FollowToken(type, split[1]);
                    switch (type) {
                        case LOCATION:
                            user.getLocationTokenList().add(followToken);
                            break;
                        case ACCOUNT:
                            user.getAccountTokenList().add(followToken);
                            break;
                        case HASHTAG:
                            user.getHashtagTokenList().add(followToken);
                            break;
                    }
                }
                if (!scheduler.checkExists(JobKey.jobKey(username)))
                    user.initJob();
            }
        }
    }

    public static String generateEmojis() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < random.nextInt(5); i++)
            ret.append(emojis[random.nextInt(6)]);
        return ret.toString();
    }
}
