package ua.nure.mpj.lb4.callbacks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ua.nure.mpj.lb4.entities.Subject;
import ua.nure.mpj.lb4.services.SubjectService;

import java.util.Optional;

@Slf4j
@Component
public class ManageSubjectCallback {
    private final SubjectService subjectService;

    @Autowired
    public ManageSubjectCallback(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    public void execute(TelegramClient client, Chat chat, long subjectId, MaybeInaccessibleMessage originMessage) {
        Optional<Subject> subjectOpt = subjectService.get(subjectId);
        if(subjectOpt.isEmpty()) {
            try {
                client.execute(EditMessageText.builder()
                        .chatId(chat.getId())
                        .messageId(originMessage.getMessageId())
                        .text("Unknown subject")
                        .build());
            } catch (TelegramApiException e) {
                log.error("Failed to send message", e);
            }
            return;
        }

        Subject subject = subjectOpt.get();

        EditMessageText request = EditMessageText.builder()
                .chatId(chat.getId())
                .messageId(originMessage.getMessageId())
                .text(String.format("""
                        Subject Info
                        
                        Id: %d
                        Name: %s
                        Short name: %s""", subject.getId(), subject.getName(), subject.getShortName()))
                .replyMarkup(InlineKeyboardMarkup.builder().keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("Edit name")
                                .callbackData("update_subject_" + subjectId)
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("Edit short name")
                                .callbackData("update_subject_short_" + subjectId)
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("Delete")
                                .callbackData("delete_subject_" + subjectId)
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
