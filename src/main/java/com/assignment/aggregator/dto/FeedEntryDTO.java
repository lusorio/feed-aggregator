package com.assignment.aggregator.dto;

import com.rometools.rome.feed.synd.SyndContent;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing a simplified RSS/ATOM feed entry used as response for
 * the {@link com.assignment.aggregator.controllers.FeedController}
 */
public class FeedEntryDTO
{

    /**
     * The link to the entry's original source
     */
    private String link;

    /**
     * The entry's publication date
     */
    private ZonedDateTime publicationDate;

    /**
     * The entry's title
     */
    private String title;

    /**
     * The entry's content
     */
    private List<SyndContent> contents = new ArrayList<>();

    /**
     * The list of entry's author(s) and collaborator(s)
     */
    private List<String> authors = new ArrayList<>();

    public String getLink()
    {
        return link;
    }

    public FeedEntryDTO setLink(String link)
    {
        this.link = link;
        return this;
    }

    public ZonedDateTime getPublicationDate()
    {
        return publicationDate;
    }

    public FeedEntryDTO setPublicationDate(ZonedDateTime publicationDate)
    {
        this.publicationDate = publicationDate;
        return this;
    }

    public String getTitle()
    {
        return title;
    }

    public FeedEntryDTO setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public List<SyndContent> getContents()
    {
        return contents;
    }

    public FeedEntryDTO setContents(List<SyndContent> contents)
    {
        this.contents = contents;
        return this;
    }

    public List<String> getAuthors()
    {
        return authors;
    }

    public FeedEntryDTO setAuthors(List<String> authors)
    {
        this.authors = authors;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        FeedEntryDTO that = (FeedEntryDTO) o;

        return new EqualsBuilder()
                       .append(getLink(), that.getLink())
                       .append(getPublicationDate(), that.getPublicationDate())
                       .append(getTitle(), that.getTitle())
                       .append(getContents(), that.getContents())
                       .append(getAuthors(), that.getAuthors())
                       .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                       .append(getLink())
                       .append(getPublicationDate())
                       .append(getTitle())
                       .append(getContents())
                       .append(getAuthors())
                       .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                       .append("link", link)
                       .append("publicationDate", publicationDate)
                       .append("title", title)
                       .append("contents", contents)
                       .append("authors", authors)
                       .toString();
    }
}
