package ru.amatemeow.vks.repository.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table(name = "posts")
@Entity
public class PostEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "post_id")
  private Integer postId;

  @Column(name = "author_id")
  private Integer authorId;

  @Column(name = "published")
  private ZonedDateTime date;

  @Type(JsonBinaryType.class)
  @Column(name = "group_info")
  private GroupAttribute group;

  @Column(name = "text")
  private String text;

  @PrePersist
  private void ensureId() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
  }
}
