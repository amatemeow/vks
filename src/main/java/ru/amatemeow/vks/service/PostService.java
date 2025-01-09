package ru.amatemeow.vks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.amatemeow.vks.interactor.dto.PostResponse;
import ru.amatemeow.vks.repository.PostRepository;
import ru.amatemeow.vks.repository.entity.PostEntity;
import ru.amatemeow.vks.service.mapper.PostEntityMapper;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {

  private final PostRepository postRepository;
  private final PostEntityMapper postMapper;

  public List<Integer> getPostIdsByGroupAlias(String groupAlias) {
    return postRepository.findPostIdsByGroupAlias(groupAlias);
  }

  @Transactional
  public PostEntity save(PostResponse post) {
    return postRepository.save(postMapper.mapToPostEntity(post));
  }
}
