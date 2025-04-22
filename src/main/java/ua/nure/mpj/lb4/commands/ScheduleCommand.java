package ua.nure.mpj.lb4.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ua.nure.mpj.lb4.callbacks.CalendarCallback;

@Slf4j
@Component
public class ScheduleCommand extends BotCommand {
    public ScheduleCommand() {
        super("schedule", "Display schedule for group");
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        if(strings.length == 0) {
            try {
                telegramClient.execute(SendMessage.builder()
                        .chatId(chat.getId())
                        .text("Group is not specified!")
                        .build());
            } catch (TelegramApiException e) {
                log.error("Couldn't invoke SendMessage!", e);
            }

            return;
        }

        CalendarCallback.processCommandOrCallbackQuery(telegramClient, chat, 0, null);
    }
}