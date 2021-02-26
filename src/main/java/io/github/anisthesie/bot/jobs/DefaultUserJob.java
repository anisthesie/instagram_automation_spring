package io.github.anisthesie.bot.jobs;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.users.UserAction;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineMedia;
import com.github.instagram4j.instagram4j.models.user.Profile;
import com.github.instagram4j.instagram4j.requests.direct.DirectGetByParticipantsRequest;
import com.github.instagram4j.instagram4j.requests.feed.FeedLocationRequest;
import com.github.instagram4j.instagram4j.requests.feed.FeedTagRequest;
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsActionRequest;
import com.github.instagram4j.instagram4j.requests.locationsearch.LocationSearchRequest;
import com.github.instagram4j.instagram4j.responses.direct.DirectThreadsResponse;
import com.github.instagram4j.instagram4j.responses.feed.FeedLocationResponse;
import com.github.instagram4j.instagram4j.responses.feed.FeedTagResponse;
import com.github.instagram4j.instagram4j.responses.feed.FeedUsersResponse;
import com.github.instagram4j.instagram4j.responses.locationsearch.LocationSearchResponse;
import io.github.anisthesie.bot.InstagramBot;
import io.github.anisthesie.db.User;
import org.quartz.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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
        if (user.isFollowing() || user.isCommenting() || user.isMessaging()) {
            final int rand = InstagramBot.random.nextInt(100);
            final IGClient client = user.getClient();
            if (rand > 60 && rand <= 80) { // LOCATION
                final int randLocation = InstagramBot.random.nextInt(user.getLocationTokenList().size());
                final String locationValue = user.getLocationTokenList().get(randLocation).getValue();
                LocationSearchResponse locationSearchResponse;
                FeedLocationResponse feedLocationResponse;

                try {
                    locationSearchResponse = new LocationSearchRequest(0d, 0d, locationValue).execute(client).get(20, TimeUnit.SECONDS);
                    feedLocationResponse = new FeedLocationRequest(locationSearchResponse.getVenues().get(0).getPk()).execute(client).get(20, TimeUnit.SECONDS);

                    commitActions(user, client, feedLocationResponse.getItems());

                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }


            }
            if (rand > 80) { // HASHTAG
                final int randHashtag = InstagramBot.random.nextInt(user.getHashtagTokenList().size());
                final String hashtagValue = user.getHashtagTokenList().get(randHashtag).getValue();
                FeedTagResponse feedTagResponse;

                try {
                    feedTagResponse = new FeedTagRequest(hashtagValue).execute(client).get(20, TimeUnit.SECONDS);

                    commitActions(user, client, feedTagResponse.getItems());

                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }

            } else { // ACCOUNT FOLLOWERS
                final int randAccount = InstagramBot.random.nextInt(user.getAccountTokenList().size());
                final String accountValue = user.getAccountTokenList().get(randAccount).getValue();

                try {

                    final UserAction userAction = client.actions().users().findByUsername(accountValue).get(20, TimeUnit.SECONDS);
                    final List<FeedUsersResponse> feedUsersResponses = userAction.followersFeed().stream().collect(Collectors.toList());

                    final FeedUsersResponse feed = feedUsersResponses.get(InstagramBot.random.nextInt(feedUsersResponses.size()));
                    final Profile profile = feed.getUsers().get(InstagramBot.random.nextInt(feed.getUsers().size()));

                    if (user.isFollowing())
                        new FriendshipsActionRequest(profile.getPk(), FriendshipsActionRequest.FriendshipsAction.CREATE)
                                .execute(client).get(20, TimeUnit.SECONDS);
                    if (user.isMessaging()) {
                        DirectThreadsResponse directThreadsResponse =
                                new DirectGetByParticipantsRequest(profile.getPk()).execute(client).get(20, TimeUnit.SECONDS);
                        if (directThreadsResponse.getThread() != null)
                            user.sendMessage(user.getCustomMessage(), directThreadsResponse.getThread().getThread_id());
                    }

                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void commitActions(User user, IGClient client, List<TimelineMedia> items) throws InterruptedException, ExecutionException, TimeoutException {
        final int randPost = InstagramBot.random.nextInt(items.size());
        final TimelineMedia timelineMedia = items.get(randPost);
        if (user.isCommenting())
            user.postComment(timelineMedia.getId(), InstagramBot.generateEmojis());
        if (user.isFollowing())
            new FriendshipsActionRequest(timelineMedia.getUser().getPk(), FriendshipsActionRequest.FriendshipsAction.CREATE)
                    .execute(client).get(20, TimeUnit.SECONDS);
        if (user.isMessaging()) {
            DirectThreadsResponse directThreadsResponse =
                    new DirectGetByParticipantsRequest(timelineMedia.getUser().getPk()).execute(client).get(20, TimeUnit.SECONDS);
            if (directThreadsResponse.getThread() != null)
                user.sendMessage(user.getCustomMessage(), directThreadsResponse.getThread().getThread_id());
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {

    }
}

