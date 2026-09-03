package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    @Test
    void toComment_shouldMapAllFields() {
        User author = makeUser(1L);
        Item item = makeItem(1L);
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Great item");

        Comment comment = CommentMapper.toComment(dto, item, author);

        assertThat(comment.getText()).isEqualTo("Great item");
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getCreated()).isNotNull();
    }

    @Test
    void toDto_shouldMapAllFields() {
        User author = makeUser(1L);
        Item item = makeItem(1L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.of(2025, 1, 1, 12, 0));

        CommentDto dto = CommentMapper.toDto(comment);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Great item");
        assertThat(dto.getAuthorName()).isEqualTo("user1");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
    }

    private User makeUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("user" + id);
        u.setEmail("user" + id + "@mail.com");
        return u;
    }

    private Item makeItem(Long id) {
        Item item = new Item();
        item.setId(id);
        item.setName("Drill");
        return item;
    }
}