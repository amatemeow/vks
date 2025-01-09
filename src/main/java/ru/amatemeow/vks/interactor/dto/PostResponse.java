package ru.amatemeow.vks.interactor.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.ZonedDateTime;

@Data
@Builder
@Jacksonized
public class PostResponse {

  @JsonProperty("id")
  private Integer id;

  @JsonProperty("date")
  private ZonedDateTime date;

  @JsonProperty("from_id")
  private String authorId;

  @JsonIgnore
  private GroupResponse group;

  @JsonProperty("text")
  private String text;
}
