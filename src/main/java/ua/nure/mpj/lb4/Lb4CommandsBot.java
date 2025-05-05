package ua.nure.mpj.lb4;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ua.nure.mpj.lb4.callbacks.CalendarCallback;
import ua.nure.mpj.lb4.callbacks.CreateUpdateGroupCallback;
import ua.nure.mpj.lb4.callbacks.CreateSubjectCallback;
import ua.nure.mpj.lb4.callbacks.ManageGroupCallback;
import ua.nure.mpj.lb4.commands.GroupsCommand;
import ua.nure.mpj.lb4.commands.ScheduleCommand;
import ua.nure.mpj.lb4.commands.StartCommand;
import ua.nure.mpj.lb4.commands.SubjectsCommand;
import ua.nure.mpj.lb4.entities.Group;
import ua.nure.mpj.lb4.entities.Subject;
import ua.nure.mpj.lb4.entities.UserState;
import ua.nure.mpj.lb4.services.GroupService;
import ua.nure.mpj.lb4.services.SubjectService;
import ua.nure.mpj.lb4.services.UserStateService;

import java.util.Optional;

import static ua.nure.mpj.lb4.utils.IntUtil.parseIntOrZero;
import static ua.nure.mpj.lb4.utils.SendMessageUtil.sendMessage;

@Slf4j
@Component
public class Lb4CommandsBot extends CommandLongPollingTelegramBot implements SpringLongPollingBot {
    private final String token;

    private final StartCommand startCommand;
    private final GroupsCommand groupsCommand;
    private final SubjectsCommand subjectsCommand;
    private final ScheduleCommand scheduleCommand;

    private final CreateUpdateGroupCallback createUpdateGroupCallback;
    private final ManageGroupCallback manageGroupCallback;
    private final CreateSubjectCallback createSubjectCallback;

    private final GroupService groupService;
    private final SubjectService subjectService;
    private final UserStateService userStateService;

    public Lb4CommandsBot(
            @Qualifier("telegramClient") TelegramClient telegramClient,
            @Value("${botToken}") String token,
            StartCommand startCommand,
            GroupsCommand groupsCommand,
            SubjectsCommand subjectsCommand,
            ScheduleCommand scheduleCommand,
            CreateUpdateGroupCallback createUpdateGroupCallback,
            CreateSubjectCallback createSubjectCallback,
            ManageGroupCallback manageGroupCallback,
            GroupService groupService,
            SubjectService subjectService,
            UserStateService userStateService) {
        super(telegramClient, false, () -> {
            return "";
        });
        this.token = token;

        this.startCommand = startCommand;
        this.groupsCommand = groupsCommand;
        this.subjectsCommand = subjectsCommand;
        this.scheduleCommand = scheduleCommand;

        this.createUpdateGroupCallback = createUpdateGroupCallback;
        this.manageGroupCallback = manageGroupCallback;
        this.createSubjectCallback = createSubjectCallback;

        this.groupService = groupService;
        this.subjectService = subjectService;
        this.userStateService = userStateService;

        registerAll(startCommand, groupsCommand, subjectsCommand, scheduleCommand);
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
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            MaybeInaccessibleMessage originMessage = update.getCallbackQuery().getMessage();
            Chat chat = originMessage.getChat();
            User user = update.getCallbackQuery().getFrom();

            if (data.equals("IGNORE")) {
                return;
            }

            if (data.startsWith("list_groups")) {
                int page = data.startsWith("list_groups_") ? parseIntOrZero(data.substring(12)) : 0;
                groupsCommand.processCommandOrCallbackQuery(telegramClient, chat, page, originMessage);
                return;
            }

            if (data.startsWith("list_subjects")) {
                int page = data.startsWith("list_subjects_") ? parseIntOrZero(data.substring(14)) : 0;
                subjectsCommand.processCommandOrCallbackQuery(telegramClient, chat, page, originMessage);
                return;
            }

            if (data.equals("create_group")) {
                createUpdateGroupCallback.execute(telegramClient, chat, user, UserState.Action.CREATE_GROUP, null);
                return;
            }

            if (data.equals("create_subject")) {
                createSubjectCallback.execute(telegramClient, chat, user);
                return;
            }

            if (data.startsWith("calendar")) {
                int monthOffset = data.startsWith("calendar_") ? parseIntOrZero(data.substring(9)) : 0;
                CalendarCallback.processCommandOrCallbackQuery(telegramClient, chat, monthOffset, originMessage);
                return;
            }

            if (data.startsWith("date_sel_")) {
                String date = data.substring(9);
                // TODO: save to user state
                try {
                    telegramClient.execute(
                            EditMessageText.builder()
                                    .chatId(chat.getId())
                                    .text("Selected date: " + date)
                                    .messageId(originMessage.getMessageId())
                                    .build()
                    );
                } catch (TelegramApiException e) {
                    log.error("Couldn't invoke SendMessage!");
                }
                return;
            }

            if(data.startsWith("manage_group_")) {
                manageGroupCallback.execute(telegramClient, chat, parseIntOrZero(data.substring(13)), originMessage);
                return;
            }

            if(data.startsWith("update_group_")) {
                createUpdateGroupCallback.execute(telegramClient, chat, update.getCallbackQuery().getFrom(), UserState.Action.UPDATE_GROUP, String.valueOf(parseIntOrZero(data.substring(13))));
                return;
            }

            if(data.startsWith("delete_group_")) {
                long groupId = parseIntOrZero(data.substring(13));
                groupService.deleteById(groupId);
                sendMessage(telegramClient, chat.getId(), "Group deleted");
                userStateService.deleteById(user.getId());
                return;
            }

            log.info("Unknown callback query data!");
            return;
        }

        if (!update.hasMessage()) {
            log.info("Unknown update!");
            return;
        }

        Message message = update.getMessage();
        Chat chat = message.getChat();
        User user = message.getFrom();

        System.out.printf("Got message from %s: %s%n", user.getFirstName(), message.getText());

        Optional<UserState> state = userStateService.get(user.getId());
        if (state.isEmpty())
            return;

        switch (state.get().getAction()) {
            case CREATE_GROUP: {
                Group group = groupService.save(new Group(message.getText()));
                sendMessage(telegramClient, chat.getId(), "Group created: " + group.getName());
                userStateService.deleteById(user.getId());
                break;
            }
            case UPDATE_GROUP: {
                if(parseIntOrZero(state.get().getData()) == 0) {
                    sendMessage(telegramClient, chat.getId(), "Invalid state, please try again");
                    return;
                }

                Optional<Group> groupOpt = groupService.get(parseIntOrZero(state.get().getData()));
                if(groupOpt.isEmpty()) {
                    sendMessage(telegramClient, chat.getId(), "Group does not exist!");
                    return;
                }

                Group group = groupOpt.get();
                group.setName(message.getText());
                groupService.save(group);

                sendMessage(telegramClient, chat.getId(), "Group updated: " + group.getName());
                userStateService.deleteById(user.getId());
            }
            case CREATE_SUBJECT: {
                if(state.get().getState() == UserState.State.WAITING_FOR_SUBJECT_NAME) {
                    UserState userState = state.get();
                    userState.setState(UserState.State.WAITING_FOR_SUBJECT_SHORT_NAME);
                    userState.setData(message.getText());
                    userStateService.save(userState);
                    sendMessage(telegramClient, chat.getId(), "Send subject short name: ");
                } else if(state.get().getState() == UserState.State.WAITING_FOR_SUBJECT_SHORT_NAME) {
                    Subject subject = subjectService.save(new Subject(state.get().getData(), message.getText()));
                    sendMessage(telegramClient, chat.getId(), "Subject created: " + subject.getName());

                    userStateService.deleteById(user.getId());
                } else {
                    sendMessage(telegramClient, chat.getId(), "Invalid state, please try again");
                    userStateService.deleteById(user.getId());
                    return;
                }
                break;
            }
        }

    }
}
