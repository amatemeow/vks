package ru.amatemeow.vks.interactor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Jacksonized
public class WallResponse {

  @NotNull
  @JsonProperty("count")
  private Integer count;

  @Builder.Default
  @JsonProperty("items")
  private List<PostResponse> posts = new ArrayList<>();

  @Builder.Default
  @JsonProperty("groups")
  private List<GroupResponse> groups = new ArrayList<>();
}
