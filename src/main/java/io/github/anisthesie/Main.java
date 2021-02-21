package io.github.anisthesie;

import io.github.anisthesie.bot.InstagramBot;
import io.github.anisthesie.db.Database;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@SpringBootApplication
public class Main {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
        Database.initDatabase();
        InstagramBot.startBot();
    }

    @GetMapping(value = "/start_targeting", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    String target(@RequestParam("username") String username) {
        InstagramBot.startTargeting(username);
        return "Success.";
    }

    @GetMapping(value = "/send_dm", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    String send_dm(@RequestParam("username") String username, @RequestParam("target") String target, @RequestParam("message") String message) {
        InstagramBot.sendDm(username, target, message);
        return "Success.";
    }

    @GetMapping(value = "/schedule_post", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    String schedule_post(@RequestParam("username") String username, @RequestParam("delay") long delay, @RequestParam("pid") String pid) {
        InstagramBot.schedulePost(username, delay, pid);
        return "Success.";
    }

}
