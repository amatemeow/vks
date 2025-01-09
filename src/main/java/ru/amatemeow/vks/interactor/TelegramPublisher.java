package ru.amatemeow.vks.interactor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class TelegramPublisher {

  private final OkHttpClient httpClient;

  @Value("${vks.connection.telegram.push.url}")
  private String pushUrl;

  @Value("${vks.connection.telegram.push.chat-id}")
  private String pushChatId;

  public void push(String message) {
    MultipartBody body = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("chat_id", pushChatId)
        .addFormDataPart("text", message)
        .build();
    Request pushRequest = new Request.Builder()
        .post(body)
        .url(pushUrl)
        .build();

    try(Response response = httpClient.newCall(pushRequest).execute()){
      if (!response.isSuccessful()) {
        log.debug("Something went wrong while sending Telegram push request ({}) to chat '{}': {}",
            pushRequest,
            pushChatId,
            response);
      }
    } catch (IOException logged) {
      log.debug("Failed to push notification ({}) to Telegram chat '{}': {}", message, pushChatId, logged.getMessage());
    }
    log.debug("Successfully sent notification to Telegram chat '{}': {}", pushChatId, message);
  }
}
