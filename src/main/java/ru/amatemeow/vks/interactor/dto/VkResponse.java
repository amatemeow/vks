package ru.amatemeow.vks.interactor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class VkResponse {

  @JsonProperty("response")
  public Object response;
}
