package io.github.anisthesie.bot;

import io.github.anisthesie.bot.actions.FollowToken;
import io.github.anisthesie.bot.actions.FollowType;
import io.github.anisthesie.db.Database;
import io.github.anisthesie.db.User;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Random;

public class InstagramBot {

    public static final HashMap<String, User> USERS = new HashMap<>();

    public static Scheduler scheduler;

    public static Random random = new Random();

    public static void startBot() {
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
        final ResultSet resultSet = statement.executeQuery("SELECT username, password, following, commenting, tokens from Users");
        while (resultSet.next()) {
            final String username = resultSet.getString("username");
            final String password = resultSet.getString("password");
            final boolean following = resultSet.getInt("following") == 1;
            final boolean commenting = resultSet.getInt("commenting") == 1;
            final String tokens = resultSet.getString("tokens");
            final User user = new User(username, password, following, commenting);
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

    public static void sendDm(String username, String target, String message) {
    }

    public static void schedulePost(String username, long delay, String pid) {
    }

    public static void startTargeting(String username) {
    }

    public static void startTargeting(String username, String password) {

    }
}
