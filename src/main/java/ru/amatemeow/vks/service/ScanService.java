package ru.amatemeow.vks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.amatemeow.vks.exception.AppError;
import ru.amatemeow.vks.interactor.TelegramPublisher;
import ru.amatemeow.vks.interactor.VkInteractor;
import ru.amatemeow.vks.interactor.dto.GroupResponse;
import ru.amatemeow.vks.interactor.dto.PostResponse;
import ru.amatemeow.vks.interactor.dto.VkResponse;
import ru.amatemeow.vks.interactor.dto.WallResponse;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScanService {

  private final VkInteractor vkInteractor;
  private final TelegramPublisher telegramPublisher;
  private final ObjectMapper objectMapper;

  @Value("${vks.task.scan-source}")
  private String scanSource;

  @Value("${vks.task.query}")
  private String searchQuery;

  @Value("${vks.task.fresh-interval}")
  private Integer postFreshnessInterval;

  private final List<Integer> foundIds = new ArrayList<>();

  @Scheduled(fixedDelayString = "${vks.task.execution-interval}", timeUnit = TimeUnit.SECONDS)
  public void scan() {
    Map<String, String> params = new HashMap<>();
    params.put("extended", "1");
    params.put("owner_id", scanSource);
    params.put("query", "\"" + searchQuery + "\"");
    params.put("owners_only", "1");

    try {
      VkResponse vkResponse = vkInteractor.get("wall.search", params);
      WallResponse wallResponse = objectMapper.convertValue(vkResponse.getResponse(), WallResponse.class);
      PostResponse foundPost = processWallResponse(wallResponse);
      if (foundPost != null) {
        String message = composeNotification(foundPost);
        telegramPublisher.push(message);
      }
    } catch (AppError logged) {
      log.error("Encountered problems while sending request to VK: {}", logged.getMessage());
    } catch (Exception logged) {
      log.error("Could not parse response from VK: {}", logged.getMessage());
    }
  }

  private PostResponse processWallResponse(WallResponse response) {
    List<PostResponse> foundPosts = new ArrayList<>();

    if (!response.getPosts().isEmpty()) {
      for (PostResponse post : response.getPosts()) {
        if (ZonedDateTime.now().isBefore(post.getDate().plusDays(postFreshnessInterval))) {
          foundPosts.add(post);
        }
      }
    }

    if (foundPosts.isEmpty()) {
      log.debug("No posts found");
    } else {
      PostResponse latestPost = foundPosts.stream().min(Comparator.comparing(PostResponse::getDate)).get();
      if (!foundIds.contains(latestPost.getId())) {
        foundIds.add(latestPost.getId());
        log.info("Found latest post at {}: {}", latestPost.getDate().format(DateTimeFormatter.ISO_ZONED_DATE_TIME), latestPost.getText());
        if (!response.getGroups().isEmpty()) {
          latestPost.setGroup(response.getGroups().get(0));
        }
        return latestPost;
      } else {
        log.debug("Latest post has been already notified of");
      }
    }
    return null;
  }

  private String composeNotification(PostResponse post) {
    return "[Новый пост]" +
        (post.getGroup() != null ? "\nГруппа: " + post.getGroup().getName() : "") +
        "\nОпубликован: " + post.getDate().format(DateTimeFormatter.ISO_ZONED_DATE_TIME) +
        "\nСсылка: " + composePostLink(post) +
        "\nТекст поста:" +
        "\n-------------\n" +
        post.getText() +
        "\n-------------";
  }

  private String composePostLink(PostResponse post) {
    GroupResponse group = post.getGroup();
    if (group == null) {
      return "";
    }
    return "vk.com/" + post.getGroup().getAlias() + "?w=wall-" + group.getId() + "_" + post.getId();
  }
}
