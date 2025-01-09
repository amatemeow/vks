package ru.amatemeow.vks.interactor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;
import ru.amatemeow.vks.configuration.Credentials;
import ru.amatemeow.vks.configuration.VkConfig;
import ru.amatemeow.vks.exception.AppAccessDeniedError;
import ru.amatemeow.vks.exception.AppExternalRequestError;
import ru.amatemeow.vks.interactor.dto.VkResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class VkInteractor {

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final VkConfig vkConfig;
  private final Credentials credentials;

  public VkResponse get(String method, Map<String, String> params) {
    String url = vkConfig.getBaseUrl() + method + "?";

    if (params == null || params.isEmpty()) {
      params = new HashMap<>();
    }

    params.put("v", vkConfig.getTargetVersion());
    String paramString = params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));

    Request request = new Request.Builder()
        .get()
        .url(url + paramString)
        .addHeader("Authorization", "Bearer " + credentials.getKey())
        .build();

    String responseBody = processRequestAndGetBody(request);
    try {
      return objectMapper.readValue(responseBody, VkResponse.class);
    } catch (JsonProcessingException logged) {
      log.error("Failed to parse VK response: {}. Response: {}", logged.getMessage(), responseBody);
    }

    return null;
  }

  private String processRequestAndGetBody(Request request) {
    try(Response response = httpClient.newCall(request).execute()) {
      String body = response.body() != null ? response.body().string() : "";

      if (!response.isSuccessful()) {
        if (response.code() == 210) {
          throw new AppAccessDeniedError("VK denied access to requested resource: " +
              body + ". Request: " + request);
        }
        throw new AppExternalRequestError("VK answered with status code " + response.code() + ". Full response: " + response);
      }

      return body;
    } catch (Exception e) {
      throw new AppExternalRequestError("Failed to send GET request to VK: " +
          e.getMessage() + ". Request: " + request, e);
    }
  }
}
