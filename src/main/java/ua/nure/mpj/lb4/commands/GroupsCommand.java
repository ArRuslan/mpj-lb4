package ua.nure.mpj.lb4.commands;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
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
import ua.nure.mpj.lb4.services.GroupService;
import ua.nure.mpj.lb4.services.UserStateService;

import static ua.nure.mpj.lb4.utils.IntUtil.parseIntOrZero;

@Slf4j
@Component
public class GroupsCommand extends BotCommand {
    private static final int PAGE_SIZE = 10;

    private final GroupService groupService;

    @Autowired
    public GroupsCommand(GroupService groupService) {
        super("groups", "List groups");

        this.groupService = groupService;
    }

    public void processCommandOrCallbackQuery(TelegramClient client, Chat chat, int page, @Nullable MaybeInaccessibleMessage originMessage) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder = InlineKeyboardMarkup.builder().keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("Create group")
                        .callbackData("create_group")
                        .build()
        ));

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("[").append(page + 1).append("] Groups:\n");
        for(Group group : groupService.list(page, PAGE_SIZE)) {
            messageBuilder.append("- ").append(group.getName()).append("\n");
            keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text(group.getName())
                            .callbackData("manage_group_"+group.getId())
                            .build()
            ));
        }

        if(groupService.count() > (long) PAGE_SIZE * (page + 1)) {
            keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text("Next page ->")
                            .callbackData(String.format("list_groups_%d", page + 1))
                            .build()
            ));
        }
        if(page > 0) {
            keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text("<- Previous page")
                            .callbackData(String.format("list_groups_%d", page - 1))
                            .build()
            ));
        }

        BotApiMethod<?> request;
        if(originMessage == null) {
            request = SendMessage.builder()
                    .chatId(chat.getId())
                    .text(messageBuilder.toString())
                    .replyMarkup(keyboardBuilder.build())
                    .build();
        } else {
            request = EditMessageText.builder()
                    .chatId(chat.getId())
                    .messageId(originMessage.getMessageId())
                    .text(messageBuilder.toString())
                    .replyMarkup(keyboardBuilder.build())
                    .build();
        }

        try {
            client.execute(request);
        } catch (TelegramApiException e) {
            log.error("Couldn't invoke SendMessage!", e);
        }
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        processCommandOrCallbackQuery(telegramClient, chat, parseIntOrZero(strings.length > 0 ? strings[0] : ""), null);
    }
}