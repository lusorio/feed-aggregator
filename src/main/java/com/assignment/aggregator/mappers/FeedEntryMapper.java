package com.assignment.aggregator.mappers;

import com.assignment.aggregator.dto.FeedEntryDTO;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeedEntryMapper extends Mapper<SyndEntry, FeedEntryDTO>
{

    private static final Logger logger = LoggerFactory.getLogger(FeedEntryMapper.class);

    /**
     * {@inheritDoc}
     * <p>
     * Converts a {@link SyndEntry}, which is the <code>rometools.rome</code> representation of a RSS/ATOM entry
     * to a {@link FeedEntryDTO}
     */
    @Override
    public FeedEntryDTO mapToDTO(SyndEntry source, Class<FeedEntryDTO> destinationType)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Mapping SyndEntry to FeedEntryDTO");
        }

        var dto = new FeedEntryDTO();
        dto.setLink(source.getLink());
        dto.setTitle(source.getTitle());
        if (logger.isInfoEnabled())
        {
            logger.info(MessageFormat.format("Entry link set to: {0}", dto.getLink()));
            logger.info(MessageFormat.format("Entry title set to: {0}", dto.getTitle()));
        }

        var entryDate = source.getPublishedDate() != null ? source.getPublishedDate() : source.getUpdatedDate();
        if (entryDate != null)
        {
            dto.setPublicationDate(entryDate.toInstant().atZone(ZoneId.systemDefault()));
            if (logger.isInfoEnabled())
            {
                logger.info(MessageFormat.format("Entry publication date set to: {0}", dto.getPublicationDate()));
            }
        }

        if (source.getAuthors() != null)
        {
            var authors = source.getAuthors().stream()
                                .map(SyndPerson::getName)
                                .collect(Collectors.toList());

            dto.getAuthors().addAll(authors);

            if (logger.isInfoEnabled())
            {
                logger.info(MessageFormat.format("Adding feed authors to authors list: {0}", authors));
            }
        }

        if (source.getContributors() != null)
        {
            var contributors = source.getContributors().stream()
                                     .map(SyndPerson::getName)
                                     .collect(Collectors.toList());

            dto.getAuthors().addAll(contributors);

            if (logger.isInfoEnabled())
            {
                logger.info(MessageFormat.format("Adding feed contributors to authors list: {0}", contributors));
            }
        }

        if (source.getDescription() != null)
        {
            dto.getContents().add(source.getDescription());

            if (logger.isInfoEnabled())
            {
                logger.info("Description found. Added to DTO contents");
            }
        }

        if (source.getContents() != null)
        {
            dto.getContents().addAll(source.getContents());
            if (logger.isInfoEnabled())
            {
                logger.info("Contents found. Added to DTO contents");
            }
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
        if (logger.isInfoEnabled())
        {
            logger.info(MessageFormat.format("Mapping {0} SyndEntry to FeedEntryDTO list", source.size()));
        }

        return source.stream()
                     .map(s -> this.mapToDTO(s, destinationType))
                     .collect(Collectors.toList());
    }
}
