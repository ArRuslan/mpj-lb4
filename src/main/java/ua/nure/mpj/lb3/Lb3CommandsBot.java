package ua.nure.mpj.lb3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ua.nure.mpj.lb3.callbacks.CreateGroupCallback;
import ua.nure.mpj.lb3.callbacks.CreateSubjectCallback;
import ua.nure.mpj.lb3.commands.GroupsCommand;
import ua.nure.mpj.lb3.commands.StartCommand;
import ua.nure.mpj.lb3.commands.SubjectsCommand;

import static ua.nure.mpj.lb3.utils.IntUtil.parseIntOrZero;

@Slf4j
@Component
public class Lb3CommandsBot extends CommandLongPollingTelegramBot implements SpringLongPollingBot {
    private final String token;

    private final StartCommand startCommand;
    private final GroupsCommand groupsCommand;
    private final SubjectsCommand subjectsCommand;

    public Lb3CommandsBot(
            @Qualifier("telegramClient") TelegramClient telegramClient,
            @Value("${botToken}") String token,
            StartCommand startCommand,
            GroupsCommand groupsCommand,
            SubjectsCommand subjectsCommand) {
        super(telegramClient, false, () -> { return ""; });
        this.token = token;

        this.startCommand = startCommand;
        this.groupsCommand = groupsCommand;
        this.subjectsCommand = subjectsCommand;

        registerAll(startCommand, groupsCommand, subjectsCommand);
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if(update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            MaybeInaccessibleMessage originMessage = update.getCallbackQuery().getMessage();
            Chat chat = originMessage.getChat();

            if(data.startsWith("list_groups")) {
                int page = data.startsWith("list_groups_") ? parseIntOrZero(data.substring(12)) : 0;
                groupsCommand.processCommandOrCallbackQuery(telegramClient, chat, page, originMessage);
                return;
            }

            if(data.startsWith("list_subjects")) {
                int page = data.startsWith("list_subjects_") ? parseIntOrZero(data.substring(14)) : 0;
                subjectsCommand.processCommandOrCallbackQuery(telegramClient, chat, page, originMessage);
                return;
            }

            if(data.equals("create_group")) {
                CreateGroupCallback.execute(telegramClient, chat);
                return;
            }

            if(data.equals("create_subject")) {
                CreateSubjectCallback.execute(telegramClient, chat);
                return;
            }

            log.info("Unknown callback query data!");
            return;
        }

        if(update.hasMessage()) {
            System.out.printf("%s: %s%n", update.getMessage().getFrom().getFirstName(), update.getMessage().getText());
        } else {
            log.info("Unknown update!");
            return;
        }

        try {
            telegramClient.execute(
                    SendMessage.builder()
                            .chatId(update.getMessage().getChatId())
                            .text(update.getMessage().getText())
                            .replyToMessageId(update.getMessage().getMessageId())
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error("Couldn't invoke SendMessage!");
        }
    }
}
