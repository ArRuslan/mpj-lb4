package ua.nure.mpj.lb4.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
public class StartCommand extends BotCommand {
    public StartCommand() {
        super("start", "Start command");
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        try {
            telegramClient.execute(
                    SendMessage.builder()
                            .chatId(chat.getId())
                            .text("""
                                    Supported commands:
                                    /groups - list groups
                                    /subjects - list subjects
                                    /schedule [group name] - list groups schedule items""")
                            .replyMarkup(InlineKeyboardMarkup.builder()
                                    .keyboardRow(new InlineKeyboardRow(
                                            InlineKeyboardButton.builder()
                                                    .text("List Groups")
                                                    .callbackData("list_groups")
                                                    .build(),
                                            InlineKeyboardButton.builder()
                                                    .text("Create Group")
                                                    .callbackData("create_group")
                                                    .build()
                                    ))
                                    .keyboardRow(new InlineKeyboardRow(
                                            InlineKeyboardButton.builder()
                                                    .text("List Subjects")
                                                    .callbackData("list_subjects")
                                                    .build(),
                                            InlineKeyboardButton.builder()
                                                    .text("Create Subject")
                                                    .callbackData("create_subject")
                                                    .build()
                                    ))
                                    .keyboardRow(new InlineKeyboardRow(
                                            InlineKeyboardButton.builder()
                                                    .text("Create schedule")
                                                    .callbackData("create_schedule")
                                                    .build()
                                    ))
                                    .build())
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error("Couldn't invoke SendMessage!", e);
        }
    }
}