package io.github.anisthesie;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.users.UserAction;
import io.github.anisthesie.bot.InstagramBot;
import io.github.anisthesie.db.Database;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@SpringBootApplication
public class Main {

    public static void main(String[] args) throws Exception {
        Database.initDatabase();
        InstagramBot.startBot();
        SpringApplication.run(Main.class, args);
    }

    @GetMapping(value = "/send_dm", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    String send_dm(@RequestParam("username") String username, @RequestParam("target") String target, @RequestParam("message") String message) {
        return InstagramBot.sendDm(username, target, message);
    }

    @GetMapping(value = "/schedule_post", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    String schedule_post(@RequestParam("username") String username, @RequestParam("delay") int delay, @RequestParam("url") String url) {
        return InstagramBot.schedulePost(username, delay, url);

    }

    @GetMapping(value = "/update_user", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    String update_user(@RequestParam("username") String username) {
        return InstagramBot.updateUser(username);
    }

}
