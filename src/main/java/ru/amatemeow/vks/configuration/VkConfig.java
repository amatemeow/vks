package ru.amatemeow.vks.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class VkConfig {

  @Value("${vks.connection.vk.api-target-version}")
  private String targetVersion;

  @Value("${vks.connection.vk.api-base-url}")
  private String baseUrl;
}
