package ru.amatemeow.vks.configuration;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class ConnectionConfig {

  @Bean
  public OkHttpClient httpClient(@Value("${vks.connection.webclient.timeout}") int timeout) {
    return new OkHttpClient.Builder()
        .connectTimeout(timeout, TimeUnit.MILLISECONDS)
        .callTimeout(timeout, TimeUnit.MILLISECONDS)
        .build();
  }

  @Bean
  public Credentials credentials(@Value("${vks.connection.vk.auth.key}") String apiKey) {
    return Credentials.builder().key(apiKey).build();
  }
}
