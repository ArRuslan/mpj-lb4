package ua.nure.mpj.lb4;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ua.nure.mpj.lb4.callbacks.*;
import ua.nure.mpj.lb4.commands.GroupsCommand;
import ua.nure.mpj.lb4.commands.ScheduleCommand;
import ua.nure.mpj.lb4.commands.StartCommand;
import ua.nure.mpj.lb4.commands.SubjectsCommand;
import ua.nure.mpj.lb4.entities.Group;
import ua.nure.mpj.lb4.entities.ScheduleItem;
import ua.nure.mpj.lb4.entities.Subject;
import ua.nure.mpj.lb4.entities.UserState;
import ua.nure.mpj.lb4.services.GroupService;
import ua.nure.mpj.lb4.services.ScheduleItemService;
import ua.nure.mpj.lb4.services.SubjectService;
import ua.nure.mpj.lb4.services.UserStateService;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

import static ua.nure.mpj.lb4.utils.IntUtil.parseIntOrZero;
import static ua.nure.mpj.lb4.utils.IntUtil.parseLongOrZero;
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
    private final ManageSubjectCallback manageSubjectCallback;
    private final CreateSubjectCallback createSubjectCallback;
    private final CreateScheduleCallback createScheduleCallback;

    private final GroupService groupService;
    private final SubjectService subjectService;
    private final UserStateService userStateService;
    private final ScheduleItemService scheduleItemService;

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
            ManageSubjectCallback manageSubjectCallback,
            CreateScheduleCallback createScheduleCallback,
            GroupService groupService,
            SubjectService subjectService,
            UserStateService userStateService,
            ScheduleItemService scheduleItemService) {
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
        this.manageSubjectCallback = manageSubjectCallback;
        this.createSubjectCallback = createSubjectCallback;
        this.createScheduleCallback = createScheduleCallback;

        this.groupService = groupService;
        this.subjectService = subjectService;
        this.userStateService = userStateService;
        this.scheduleItemService = scheduleItemService;

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

            if(data.startsWith("manage_subject_")) {
                manageSubjectCallback.execute(telegramClient, chat, parseIntOrZero(data.substring(15)), originMessage);
                return;
            }

            if(data.startsWith("update_subject_")) {
                boolean shortName = data.startsWith("update_subject_short_");
                int subjectIdOffset = shortName ? 21 : 15;
                UserState.State actionState = shortName ? UserState.State.WAITING_FOR_SUBJECT_SHORT_NAME : UserState.State.WAITING_FOR_SUBJECT_NAME;

                String subjectIdStr = String.valueOf(parseIntOrZero(data.substring(subjectIdOffset)));
                userStateService.setState(user.getId(), UserState.Action.UPDATE_SUBJECT, actionState, subjectIdStr);

                sendMessage(telegramClient, chat.getId(), shortName ? "Send subject short name: " : "Send subject name: ");
                return;
            }

            if(data.startsWith("delete_subject_")) {
                long subjectId = parseIntOrZero(data.substring(15));
                subjectService.deleteById(subjectId);
                sendMessage(telegramClient, chat.getId(), "Subject deleted");
                userStateService.deleteById(user.getId());
                return;
            }

            if(data.startsWith("create_schedule")) {
                // TODO: allow to pass group id like create_schedule_{GROUP_ID}
                createScheduleCallback.execute(telegramClient, chat, user);
                return;
            }

            if(data.startsWith("date_sel_")) {
                Optional<UserState> stateOpt = userStateService.get(user.getId());
                if (stateOpt.isEmpty())
                    return;
                UserState state = stateOpt.get();
                if(state.getAction() != UserState.Action.CREATE_SCHEDULE || state.getState() != UserState.State.WAITING_FOR_SCHEDULE_DATE)
                    return;

                try {
                    Date date = CalendarCallback.DATE_FMT.parse(data.substring(9));
                    state.setState(UserState.State.WAITING_FOR_SCHEDULE_POSITION);
                    state.setData(state.getData() + ";" + date.getTime());
                    userStateService.save(state);
                    sendMessage(telegramClient, chat.getId(), "Send schedule item position (1-7): ");
                } catch (ParseException e) {
                    sendMessage(telegramClient, chat.getId(), "Invalid date ?");
                }
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
            case UPDATE_SUBJECT: {
                if(parseIntOrZero(state.get().getData()) == 0) {
                    sendMessage(telegramClient, chat.getId(), "Invalid state, please try again");
                    return;
                }

                Optional<Subject> subjectOpt = subjectService.get(parseIntOrZero(state.get().getData()));
                if(subjectOpt.isEmpty()) {
                    sendMessage(telegramClient, chat.getId(), "Subject does not exist!");
                    return;
                }

                Subject subject = subjectOpt.get();

                if(state.get().getState() == UserState.State.WAITING_FOR_SUBJECT_NAME) {
                    subject.setName(message.getText());
                    subjectService.save(subject);

                    sendMessage(telegramClient, chat.getId(), "Subject name updated: " + subject.getName());
                    userStateService.deleteById(user.getId());
                } else if(state.get().getState() == UserState.State.WAITING_FOR_SUBJECT_SHORT_NAME) {
                    subject.setShortName(message.getText());
                    subjectService.save(subject);

                    sendMessage(telegramClient, chat.getId(), "Subject short name updated: " + subject.getShortName());
                    userStateService.deleteById(user.getId());
                } else {
                    sendMessage(telegramClient, chat.getId(), "Invalid state, please try again");
                }
                userStateService.deleteById(user.getId());
                break;
            }
            case CREATE_SCHEDULE: {
                if(state.get().getState() == UserState.State.WAITING_FOR_SCHEDULE_GROUP_NAME) {
                    Optional<Group> group = groupService.findByName(message.getText());
                    if(group.isEmpty()) {
                        sendMessage(telegramClient, chat.getId(), "Group does not exist!");
                        return;
                    }

                    UserState userState = state.get();
                    userState.setState(UserState.State.WAITING_FOR_SCHEDULE_SUBJECT_NAME);
                    userState.setData(String.valueOf(group.get().getId()));
                    userStateService.save(userState);
                    sendMessage(telegramClient, chat.getId(), "Send subject short name: ");
                } else if(state.get().getState() == UserState.State.WAITING_FOR_SCHEDULE_SUBJECT_NAME) {
                    Optional<Subject> subject = subjectService.findByShortName(message.getText());
                    if (subject.isEmpty()) {
                        sendMessage(telegramClient, chat.getId(), "Subject does not exist!");
                        return;
                    }

                    UserState userState = state.get();
                    userState.setState(UserState.State.WAITING_FOR_SCHEDULE_DATE);
                    userState.setData(userState.getData() + ";" + subject.get().getId());
                    userStateService.save(userState);
                    sendMessage(telegramClient, chat.getId(), "Send subject short name: ");

                    CalendarCallback.processCommandOrCallbackQuery(telegramClient, chat, 0, null);
                } else if(state.get().getState() == UserState.State.WAITING_FOR_SCHEDULE_POSITION) {
                    int pos = parseIntOrZero(message.getText());
                    if(pos <= 0 || pos > 7) {
                        sendMessage(telegramClient, chat.getId(), "Invalid position! Only numbers 1-7 are allowed.");
                        return;
                    }

                    UserState userState = state.get();
                    userState.setState(UserState.State.WAITING_FOR_SCHEDULE_TYPE);
                    userState.setData(userState.getData() + ";" + pos);
                    userStateService.save(userState);
                    sendMessage(telegramClient, chat.getId(), "Send schedule item type (LECTURE/PRACTICE/LAB/EXAM): ");
                } else if(state.get().getState() == UserState.State.WAITING_FOR_SCHEDULE_TYPE) {
                    ScheduleItem.Type type;
                    try {
                        type = ScheduleItem.Type.valueOf(message.getText());
                    } catch (IllegalArgumentException ignored) {
                        sendMessage(telegramClient, chat.getId(), "Invalid type! Allowed values: LECTURE/PRACTICE/LAB/EXAM.");
                        return;
                    }

                    String[] args = state.get().getData().split(";");
                    if(args.length != 4) {
                        sendMessage(telegramClient, chat.getId(), "Invalid state, please try again");
                        userStateService.deleteById(user.getId());
                        return;
                    }

                    Optional<Group> group = groupService.get(parseIntOrZero(args[0]));
                    Optional<Subject> subject = subjectService.get(parseIntOrZero(args[1]));
                    if(group.isEmpty() || subject.isEmpty()) {
                        sendMessage(telegramClient, chat.getId(), "Invalid state (group or subject does not exist anymore), please try again");
                        userStateService.deleteById(user.getId());
                        return;
                    }

                    java.sql.Date date = new java.sql.Date(parseLongOrZero(args[2]));

                    scheduleItemService.save(new ScheduleItem(group.get(), subject.get(), date, (byte)parseIntOrZero(args[3]), type));
                    sendMessage(telegramClient, chat.getId(), "Schedule item created!");

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
