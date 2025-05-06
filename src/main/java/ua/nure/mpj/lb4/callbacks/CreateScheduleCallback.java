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
public class CreateScheduleCallback {
    private final UserStateService userStateService;

    @Autowired
    public CreateScheduleCallback(UserStateService userStateService) {
        this.userStateService = userStateService;
    }

    public void execute(TelegramClient client, Chat chat, User user) {
        userStateService.setState(user.getId(), UserState.Action.CREATE_SCHEDULE, UserState.State.WAITING_FOR_SCHEDULE_GROUP_NAME, null);
        sendMessage(client, chat.getId(), "Send target group name: ");
    }
}
