package ua.nure.mpj.lb4.callbacks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ua.nure.mpj.lb4.entities.UserState;
import ua.nure.mpj.lb4.services.UserStateService;

import static ua.nure.mpj.lb4.utils.SendMessageUtil.sendMessage;

@Slf4j
@Component
public class CreateSubjectCallback {
    private final UserStateService userStateService;

    @Autowired
    public CreateSubjectCallback(UserStateService userStateService) {
        this.userStateService = userStateService;
    }

    public void execute(TelegramClient client, Chat chat, User user) {
        userStateService.setState(user.getId(), UserState.Action.CREATE_SUBJECT, UserState.State.WAITING_FOR_SUBJECT_NAME, null);
        sendMessage(client, chat.getId(), "Send subject name: ");
    }
}
