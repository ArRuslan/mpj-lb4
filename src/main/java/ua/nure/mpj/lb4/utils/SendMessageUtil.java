package ua.nure.mpj.lb4.utils;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
public class SendMessageUtil {
    public static Message sendMessage(TelegramClient client, long chatId, String text) {
        try {
            return client.execute(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text(text)
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error("Couldn't invoke SendMessage!");
            return null;
        }
    }
}
