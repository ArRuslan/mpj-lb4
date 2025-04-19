package ua.nure.mpj.lb3.callbacks;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
public class CreateGroupCallback {
    public static void execute(TelegramClient client, Chat chat) {
        // TODO: save user state (lb4)

        try {
            client.execute(SendMessage.builder()
                    .chatId(chat.getId())
                    .text("Send group name: ")
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }
}
