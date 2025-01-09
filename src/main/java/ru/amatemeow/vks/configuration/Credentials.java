package ru.amatemeow.vks.configuration;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Credentials {

  private String key;
}
