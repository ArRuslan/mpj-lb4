package ua.nure.mpj.lb4.callbacks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ua.nure.mpj.lb4.entities.Group;
import ua.nure.mpj.lb4.entities.ScheduleItem;
import ua.nure.mpj.lb4.entities.Subject;
import ua.nure.mpj.lb4.services.GroupService;
import ua.nure.mpj.lb4.services.ScheduleItemService;
import ua.nure.mpj.lb4.services.SubjectService;

import java.sql.Date;
import java.util.Optional;

@Slf4j
@Component
public class ViewScheduleCallback {
    private final GroupService groupService;
    private final ScheduleItemService scheduleItemService;

    @Autowired
    public ViewScheduleCallback(GroupService groupService, ScheduleItemService scheduleItemService) {
        this.groupService = groupService;
        this.scheduleItemService = scheduleItemService;
    }

    public void execute(TelegramClient client, Chat chat, long groupId, Date date, MaybeInaccessibleMessage originMessage) {
        Optional<Group> group = groupService.get(groupId);
        if(group.isEmpty()) {
            try {
                client.execute(EditMessageText.builder()
                        .chatId(chat.getId())
                        .messageId(originMessage.getMessageId())
                        .text("Unknown group")
                        .build());
            } catch (TelegramApiException e) {
                log.error("Failed to send message", e);
            }
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb
                .append("Group: ")
                .append(group.get().getName())
                .append("\n")
                .append("Date: ")
                .append(CalendarCallback.DATE_FMT.format(date))
                .append("\n\n");

        Page<ScheduleItem> scheduleItems = scheduleItemService.list(group.get(), date, 0, 32);
        for(ScheduleItem item : scheduleItems) {
            Subject subject = item.getSubject();
            sb
                    .append("Subject: ")
                    .append(subject.getName())
                    .append(" (")
                    .append(subject.getShortName())
                    .append(")\n")
                    .append("Position: ")
                    .append(item.getPosition())
                    .append("\n")
                    .append("Type: ")
                    .append(item.getType())
                    .append("\n\n")
            ;
        }

        EditMessageText request = EditMessageText.builder()
                .chatId(chat.getId())
                .messageId(originMessage.getMessageId())
                .text(sb.toString())
                .build();

        try {
            client.execute(request);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }
}
