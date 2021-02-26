package io.github.anisthesie.bot.jobs;

import io.github.anisthesie.bot.InstagramBot;
import io.github.anisthesie.db.User;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class SchedulePostJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        final String username = jobExecutionContext.getJobDetail().getKey().getName().split(":")[1];
        if (InstagramBot.USERS.containsKey(username)) {
            final User user = InstagramBot.USERS.get(username);
            if (!user.hasScheduledPost()) return;
            try {
                URL url = new URL(user.getScheduledPost().getURL());
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("User-Agent", "Firefox");

                try (InputStream inputStream = conn.getInputStream()) {
                    int n = 0;
                    byte[] buffer = new byte[1024];
                    while (-1 != (n = inputStream.read(buffer))) {
                        output.write(buffer, 0, n);
                    }
                }
                byte[] img = output.toByteArray();
                System.out.println("Posting image.");
                user.getClient().actions().timeline().uploadPhoto(img, "").join();
                System.out.println("Image posted.");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
