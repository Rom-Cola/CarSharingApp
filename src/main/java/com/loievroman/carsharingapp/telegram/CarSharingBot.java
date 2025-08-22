package com.loievroman.carsharingapp.telegram;

import com.loievroman.carsharingapp.exception.TelegramNotificationException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class CarSharingBot extends TelegramLongPollingBot {

    private final String botUsername = "CarSharing_CarSharingAppBot";

    private final String botToken = "8280364539:AAG-r_xJzxTS-cuoJxPrIfZNqamp9JOy6Fo";

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {

    }

    public void sendMessage(String chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.setParseMode("Markdown");

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new TelegramNotificationException("Can't send message to Telegram", e);
        }
    }
}
