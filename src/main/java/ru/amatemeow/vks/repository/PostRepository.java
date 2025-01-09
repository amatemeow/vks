package ru.amatemeow.vks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.amatemeow.vks.repository.entity.PostEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, UUID> {

  @Query(nativeQuery = true, value = "select p.post_id from posts p where jsonb_extract_path_text(p.group_info, 'alias') like concat('%',:groupAlias,'%')")
  List<Integer> findPostIdsByGroupAlias(String groupAlias);
}
