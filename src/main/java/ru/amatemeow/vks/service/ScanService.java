package ru.amatemeow.vks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.amatemeow.vks.enumeration.ScanMode;
import ru.amatemeow.vks.exception.AppError;
import ru.amatemeow.vks.interactor.TelegramPublisher;
import ru.amatemeow.vks.interactor.VkInteractor;
import ru.amatemeow.vks.interactor.dto.GroupResponse;
import ru.amatemeow.vks.interactor.dto.PostResponse;
import ru.amatemeow.vks.interactor.dto.VkResponse;
import ru.amatemeow.vks.interactor.dto.WallResponse;
import ru.amatemeow.vks.util.StringUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScanService {

  private final VkInteractor vkInteractor;
  private final TelegramPublisher telegramPublisher;
  private final ObjectMapper objectMapper;
  private final PostService postService;

  @Value(("${vks.task.scan.mode}"))
  private ScanMode scanMode;

  @Value("${vks.task.scan.source}")
  private String scanSource;

  @Value("${vks.task.query}")
  private String searchQuery;

  @Value("${vks.task.search-list}")
  private List<String> searchList;

  @Value("${vks.task.fresh-interval}")
  private Integer postFreshnessInterval;

  private final List<Integer> foundIds = new ArrayList<>();

  @PostConstruct
  private void init() {
    searchQuery = searchQuery.toLowerCase();
    searchList = searchList.stream().map(String::toLowerCase).toList();
    foundIds.addAll(postService.getPostIdsByGroupAlias(scanSource));
  }

  @Scheduled(fixedDelayString = "${vks.task.execution-interval}", timeUnit = TimeUnit.SECONDS)
  public void scan() {
    switch (scanMode) {
      case NEW -> scanNew();
      case SEARCH, SEARCH_ADVANCED -> scanSearch();
    }
  }

  private void scanNew() {
    Map<String, String> params = new HashMap<>();
    params.put("extended", "1");
    params.put("owner_id", scanSource);
    params.put("owners_only", "1");

    doScan("wall.get", params);
  }

  private void scanSearch() {
    String method = scanMode == ScanMode.SEARCH ? "wall.search" : "wall.get";

    Map<String, String> params = new HashMap<>();
    params.put("extended", "1");
    params.put("owner_id", scanSource);
    params.put("owners_only", "1");

    if (scanMode == ScanMode.SEARCH) {
      params.put("query", "\"" + searchQuery + "\"");
    }

    doScan(method, params);
  }

  private void doScan(String method, Map<String, String> params) {
    try {
      VkResponse vkResponse = vkInteractor.get(method, params);
      WallResponse wallResponse = objectMapper.convertValue(vkResponse.getResponse(), WallResponse.class);
      List<PostResponse> foundPosts = processWallResponse(wallResponse);
      foundPosts.stream().map(this::composeNotification).forEach(telegramPublisher::push);
    } catch (AppError logged) {
      log.error("Encountered problems while sending request to VK: {}", logged.getMessage());
    } catch (Exception logged) {
      log.error("Could not parse response from VK: {}", logged.getMessage());
    }
  }

  private List<PostResponse> processWallResponse(WallResponse response) {
    List<PostResponse> foundPosts = new ArrayList<>();

    if (!response.getPosts().isEmpty()) {
      for (PostResponse post : response.getPosts()) {
        if (post.getText() != null && ZonedDateTime.now().minusDays(postFreshnessInterval).isBefore(post.getDate())) {
          foundPosts.add(post);
        }
      }
    }

    if (foundPosts.isEmpty()) {
      log.debug("No posts found");
      return List.of();
    }

    List<PostResponse> processedPosts = switch (scanMode) {
      case NEW -> processNewPosts(foundPosts);
      case SEARCH -> {
        PostResponse foundPost = processSimpleSearchPosts(foundPosts);
        yield foundPost == null ? List.of() : List.of(foundPost);
      }
      case SEARCH_ADVANCED -> processAdvancedSearchPosts(foundPosts);
    };

    GroupResponse group = response.getGroups().isEmpty() ? null : response.getGroups().getFirst();
    if (group != null) {
      processedPosts.forEach(p -> p.setGroup(group));
    }
    processedPosts.forEach(postService::save);
    foundIds.addAll(processedPosts.stream().map(PostResponse::getId).toList());

    return processedPosts;
  }

  private List<PostResponse> processNewPosts(List<PostResponse> foundPosts) {
    List<PostResponse> newestPosts = foundPosts.stream().filter(p -> !foundIds.contains(p.getId())).toList();
    if (newestPosts.isEmpty()) {
      log.debug("Newest posts have been already notified of");
    } else {
      String postsLine = newestPosts.stream().map(p -> String.format("Id: %s, Date: %s, Text: %s",
          p.getId(), p.getDate().format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
          p.getText().replaceAll("\n", " "))).collect(Collectors.joining("\n\t"));
      log.info("Found new posts:\n\t{}", postsLine);
    }
    return newestPosts;
  }

  private PostResponse processSimpleSearchPosts(List<PostResponse> foundPosts) {
    PostResponse latestPost = foundPosts.stream().min(Comparator.comparing(PostResponse::getDate)).get();
    if (!foundIds.contains(latestPost.getId())) {
      log.info("Found latest post at {}: {}", latestPost.getDate().format(DateTimeFormatter.ISO_ZONED_DATE_TIME), latestPost.getText());
      return latestPost;
    } else {
      log.debug("Latest post has been already notified of");
    }
    return null;
  }

  private List<PostResponse> processAdvancedSearchPosts(List<PostResponse> foundPosts) {
    if (searchList.isEmpty()) {
      log.debug("Search list is empty");
      return List.of();
    }

    List<PostResponse> latestPosts = foundPosts.stream()
        .filter(p -> !foundIds.contains(p.getId()))
        .filter(p -> StringUtils.containsAny(p.getText(), searchList, false))
        .toList();

    if (latestPosts.isEmpty()) {
      log.debug("Latest posts have been already notified of or no search criteria has been met");
    } else {
      String postsLine = latestPosts.stream().map(p -> String.format("Id: %s, Date: %s, Text: %s",
          p.getId(), p.getDate().format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
          p.getText().replaceAll("\n", " "))).collect(Collectors.joining("\n\t"));
      log.info("Found latest posts:\n\t{}", postsLine);
    }
    return latestPosts;
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
