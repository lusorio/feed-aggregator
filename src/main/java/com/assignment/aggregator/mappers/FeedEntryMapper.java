package com.assignment.aggregator.mappers;

import com.assignment.aggregator.dto.FeedEntryDTO;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndPerson;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeedEntryMapper extends Mapper<SyndEntry, FeedEntryDTO>
{

    /**
     * {@inheritDoc}
     * <p>
     * Converts a {@link SyndEntry}, which is the <code>rometools.rome</code> representation of a RSS/ATOM entry
     * to a {@link FeedEntryDTO}
     */
    @Override
    public FeedEntryDTO mapToDTO(SyndEntry source, Class<FeedEntryDTO> destinationType)
    {
        var dto = new FeedEntryDTO();
        dto.setLink(source.getLink());
        dto.setTitle(source.getTitle());

        var entryDate = source.getPublishedDate() != null ? source.getPublishedDate() : source.getUpdatedDate();
        if (entryDate != null)
        {
            dto.setPublicationDate(entryDate.toInstant().atZone(ZoneId.systemDefault()));
        }

        if (source.getAuthors() != null)
        {
            dto.getAuthors().addAll(source.getAuthors().stream()
                                          .map(SyndPerson::getName)
                                          .collect(Collectors.toList()));
        }

        if (source.getContributors() != null)
        {
            dto.getAuthors().addAll(source.getContributors().stream().map(SyndPerson::getName).collect(Collectors.toList()));
        }

        if (source.getDescription() != null)
        {
            dto.getContents().add(source.getDescription());
        }

        if (source.getContents() != null)
        {
            dto.getContents().addAll(source.getContents());
        }

        return dto;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Converts a {@link List} of {@link SyndEntry}, which is the <code>rometools.rome</code> representation of a RSS/ATOM entry
     * to a {@link FeedEntryDTO}
     */
    @Override
    public List<FeedEntryDTO> mapToDTO(Collection<SyndEntry> source, Class<FeedEntryDTO> destinationType)
    {
        return source.stream()
                     .map(s -> this.mapToDTO(s, destinationType))
                     .collect(Collectors.toList());
    }
}
