package ua.nure.mpj.lb4.callbacks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ua.nure.mpj.lb4.entities.UserState;
import ua.nure.mpj.lb4.services.UserStateService;

@Slf4j
@Component
public class CreateGroupCallback {
    private final UserStateService userStateService;

    @Autowired
    public CreateGroupCallback(UserStateService userStateService) {
        this.userStateService = userStateService;
    }

    public void execute(TelegramClient client, Chat chat, User user) {
        userStateService.setState(user.getId(), UserState.Action.CREATE_GROUP, UserState.State.WAITING_FOR_GROUP_NAME, null);

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
