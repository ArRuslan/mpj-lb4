package ua.nure.mpj.lb4;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Lb4BotConfiguration {
    @Bean
    public TelegramClient telegramClient(@Value("${botToken}") String botToken) {
        return new OkHttpTelegramClient(botToken);
    }

    @Bean("telegramBotsApplication")
    public TelegramBotsLongPollingApplication telegramBotsApplication() {
        return new TelegramBotsLongPollingApplication();
    }

    @Bean("usersSelectedGroups")
    @Scope("singleton")
    public Map<Long, Long> usersGroups() {
        return new HashMap<>();
    }
}