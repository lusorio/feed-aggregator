package com.assignment.aggregator.mappers;

import com.assignment.aggregator.AbstractSpringTest;
import com.assignment.aggregator.dto.FeedEntryDTO;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndPersonImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FeedMapperTest extends AbstractSpringTest
{

    @InjectMocks
    private FeedEntryMapper mapper;

    @Test
    void testMapToDTO_SingleElement()
    {
        var now = new Date();

        var entry = new SyndEntryImpl();
        entry.setLink("url");
        entry.setTitle("title");
        entry.setPublishedDate(now);

        var author = new SyndPersonImpl();
        author.setName("auth 1");

        entry.setAuthors(List.of(author));

        var contributor = new SyndPersonImpl();
        contributor.setName("auth 2");

        entry.setContributors(List.of(contributor));

        var description = new SyndContentImpl();
        description.setType("html");
        description.setValue("description");

        entry.setDescription(description);

        var content = new SyndContentImpl();
        content.setType("html");
        content.setValue("content");

        entry.setContents(List.of(content));

        var result = mapper.mapToDTO(entry, FeedEntryDTO.class);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(entry.getLink(), result.getLink()),
                () -> assertEquals(entry.getTitle(), result.getTitle()),
                () -> assertEquals(entry.getPublishedDate().toInstant(), Instant.from(result.getPublicationDate())),
                () -> assertEquals(2, result.getAuthors().size()),
                () -> assertTrue(result.getAuthors().containsAll(List.of("auth 1", "auth 2"))),
                () -> assertEquals(2, result.getContents().size()),
                () -> assertTrue(result.getContents().containsAll(List.of(description, content))));
    }

    @Test
    void testMapToDTO_ListOfElements()
    {
        var now = new Date();

        var entry = new SyndEntryImpl();
        entry.setLink("url");
        entry.setTitle("title");
        entry.setPublishedDate(now);

        var author = new SyndPersonImpl();
        author.setName("auth 1");

        entry.setAuthors(List.of(author));

        var contributor = new SyndPersonImpl();
        contributor.setName("auth 2");

        entry.setContributors(List.of(contributor));

        var description = new SyndContentImpl();
        description.setType("html");
        description.setValue("description");

        entry.setDescription(description);

        var content = new SyndContentImpl();
        content.setType("html");
        content.setValue("content");

        entry.setContents(List.of(content));

        var result = mapper.mapToDTO(List.of(entry, entry, entry), FeedEntryDTO.class);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(3, result.size()));
    }
}