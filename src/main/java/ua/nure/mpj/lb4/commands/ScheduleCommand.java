package ua.nure.mpj.lb4.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ua.nure.mpj.lb4.callbacks.CalendarCallback;
import ua.nure.mpj.lb4.entities.Group;
import ua.nure.mpj.lb4.entities.UserState;
import ua.nure.mpj.lb4.services.GroupService;
import ua.nure.mpj.lb4.services.UserStateService;

import java.util.Optional;

import static ua.nure.mpj.lb4.utils.SendMessageUtil.sendMessage;

@Slf4j
@Component
public class ScheduleCommand extends BotCommand {
    private final GroupService groupService;
    private final UserStateService userStateService;

    @Autowired
    public ScheduleCommand(GroupService groupService, UserStateService userStateService) {
        super("schedule", "Display schedule for group");
        this.groupService = groupService;
        this.userStateService = userStateService;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        if(strings.length == 0) {
            sendMessage(telegramClient, chat.getId(), "Group is not specified!");
            return;
        }

        String groupName = String.join(" ", strings);
        Optional<Group> group = groupService.findByName(groupName);
        if(group.isEmpty()) {
            sendMessage(telegramClient, chat.getId(), "Group does not exist!");
            return;
        }

        userStateService.setState(user.getId(), UserState.Action.VIEW_SCHEDULE, UserState.State.WAITING_FOR_SCHEDULE_DATE, String.valueOf(group.get().getId()));
        CalendarCallback.processCommandOrCallbackQuery(telegramClient, chat, 0, null);
    }
}