package ru.amatemeow.vks.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import ru.amatemeow.vks.interactor.dto.PostResponse;
import ru.amatemeow.vks.repository.entity.PostEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostEntityMapper {

  @Mappings({
      @Mapping(source = "id", target = "postId"),
      @Mapping(target = "id", ignore = true)
  })
  PostEntity mapToPostEntity(PostResponse post);
}
