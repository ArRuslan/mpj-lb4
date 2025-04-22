package ua.nure.mpj.lb4.callbacks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ua.nure.mpj.lb4.entities.Group;
import ua.nure.mpj.lb4.entities.UserState;
import ua.nure.mpj.lb4.services.GroupService;
import ua.nure.mpj.lb4.services.UserStateService;

import java.util.Optional;

@Slf4j
@Component
public class ManageGroupCallback {
    private final GroupService groupService;

    @Autowired
    public ManageGroupCallback(GroupService groupService) {
        this.groupService = groupService;
    }

    public void execute(TelegramClient client, Chat chat, long groupId, MaybeInaccessibleMessage originMessage) {
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

        EditMessageText request = EditMessageText.builder()
                .chatId(chat.getId())
                .messageId(originMessage.getMessageId())
                .text(String.format("""
                        Group Info
                        
                        Id: %d
                        Name: %s""", group.get().getId(), group.get().getName()))
                .replyMarkup(InlineKeyboardMarkup.builder().keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("Edit name")
                                .callbackData("update_group_" + groupId)
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("Delete")
                                .callbackData("delete_group_" + groupId)
                                .build()
                )).build())
                .build();

        try {
            client.execute(request);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }
}
