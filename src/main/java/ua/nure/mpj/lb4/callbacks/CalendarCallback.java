package ua.nure.mpj.lb4.callbacks;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
public class CalendarCallback {
    private static final String[] WEEK_DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private static final SimpleDateFormat MONTH_YEAR_FMT = new SimpleDateFormat("MMMMM yyyy");
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd.MM.yyyy");

    public static void processCommandOrCallbackQuery(TelegramClient client, Chat chat, long monthOffset, MaybeInaccessibleMessage originMessage) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, (int)monthOffset);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder = InlineKeyboardMarkup.builder();
        keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(MONTH_YEAR_FMT.format(calendar.getTime()))
                        .callbackData("IGNORE")
                        .build()
        ));

        InlineKeyboardRow currentRow = new InlineKeyboardRow();
        for(int i = 0; i < 7; i++) {
            currentRow.add(InlineKeyboardButton.builder()
                    .text(WEEK_DAYS[i])
                    .callbackData("IGNORE")
                    .build());
        }

        keyboardBuilder.keyboardRow(currentRow);
        currentRow = new InlineKeyboardRow();

        int startPadding = Math.floorMod((calendar.get(Calendar.DAY_OF_WEEK) - 2), 7);
        for(int i = 0; i < startPadding; i++) {
            currentRow.add(InlineKeyboardButton.builder()
                    .text("-")
                    .callbackData("IGNORE")
                    .build());
        }

        int daysCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int day = 1;
        while(day <= daysCount) {
            calendar.set(Calendar.DAY_OF_MONTH, day++);
            currentRow.add(InlineKeyboardButton.builder()
                    .text(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)))
                    .callbackData("date_sel_" + DATE_FMT.format(calendar.getTime()))
                    .build());

            if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                keyboardBuilder.keyboardRow(currentRow);
                currentRow = new InlineKeyboardRow();
            }
        }

        int lastDay = calendar.get(Calendar.DAY_OF_WEEK);
        if(lastDay > Calendar.SUNDAY) {
            int endPadding = 7 - lastDay + 1;
            for(int i = 0; i < endPadding; i++) {
                currentRow.add(InlineKeyboardButton.builder()
                        .text("-")
                        .callbackData("IGNORE")
                        .build());
            }
        }

        keyboardBuilder.keyboardRow(currentRow);
        keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("<- Previous month")
                        .callbackData("calendar_" + (monthOffset - 1))
                        .build(),
                InlineKeyboardButton.builder()
                        .text("Next month ->")
                        .callbackData("calendar_" + (monthOffset + 1))
                        .build()
        ));

        BotApiMethod<?> request;
        if(originMessage == null) {
            request = SendMessage.builder()
                    .chatId(chat.getId())
                    .text("Select day")
                    .replyMarkup(keyboardBuilder.build())
                    .build();
        } else {
            request = EditMessageText.builder()
                    .chatId(chat.getId())
                    .messageId(originMessage.getMessageId())
                    .text("Select day")
                    .replyMarkup(keyboardBuilder.build())
                    .build();
        }

        try {
            client.execute(request);
        } catch (TelegramApiException e) {
            log.error("Couldn't invoke SendMessage!", e);
        }

    }
}
