package io.github.anisthesie.bot.jobs;

import io.github.anisthesie.bot.InstagramBot;
import io.github.anisthesie.bot.actions.FollowType;
import io.github.anisthesie.db.User;
import org.quartz.*;

public class DefaultUserJob implements Job, InterruptableJob {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        final String username = jobExecutionContext.getJobDetail().getKey().getName();
        if (!InstagramBot.USERS.containsKey(username))
            return;
        final User user = InstagramBot.USERS.get(username);
        if (!user.isLogged()) {
            if (!user.login()) {
                user.incrementFailure();
                if (user.getFailure() >= 3) {
                    InstagramBot.USERS.remove(username);
                    try {
                        InstagramBot.scheduler.interrupt(username);
                        InstagramBot.scheduler.deleteJob(jobExecutionContext.getJobDetail().getKey());
                    } catch (SchedulerException e) {
                        e.printStackTrace();
                    }

                }
                return;
            }
        }
        if (user.isFollowing() || user.isCommenting()) {
            final int rand = InstagramBot.random.nextInt(100);
            if (rand > 60 && rand <= 80) {
                // select random location and random user and follow him and comment
            }
            if (rand > 80) {
                // hashtag
            } else {
                // follow account
            }
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {

    }
}
