package com.assignment.aggregator.models;

import com.rometools.rome.feed.synd.SyndContent;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FeedEntry
{

    @Id
    public String id;

    /**
     * The id of the channel
     */
    private long channelId;

    /**
     * Flags if the entry has already been read (served to the client)
     */
    private boolean fresh = false;

    /**
     * The link to the entry's original source
     */
    private String link;

    /**
     * The entry's publication date
     */
    private Instant publicationDate;

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

    public String getId()
    {
        return id;
    }

    public FeedEntry setId(String id)
    {
        this.id = id;
        return this;
    }

    public long getChannelId()
    {
        return channelId;
    }

    public FeedEntry setChannelId(long channelId)
    {
        this.channelId = channelId;
        return this;
    }

    public boolean isFresh()
    {
        return fresh;
    }

    public FeedEntry setFresh(boolean fresh)
    {
        this.fresh = fresh;
        return this;
    }

    public String getLink()
    {
        return link;
    }

    public FeedEntry setLink(String link)
    {
        this.link = link;
        return this;
    }

    public Instant getPublicationDate()
    {
        return publicationDate;
    }

    public FeedEntry setPublicationDate(Instant publicationDate)
    {
        this.publicationDate = publicationDate;
        return this;
    }

    public String getTitle()
    {
        return title;
    }

    public FeedEntry setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public List<SyndContent> getContents()
    {
        return contents;
    }

    public FeedEntry setContents(List<SyndContent> contents)
    {
        this.contents = contents;
        return this;
    }

    public List<String> getAuthors()
    {
        return authors;
    }

    public FeedEntry setAuthors(List<String> authors)
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

        FeedEntry feedEntry = (FeedEntry) o;

        return new EqualsBuilder()
                       .append(getLink(), feedEntry.getLink())
                       .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                       .append(getLink())
                       .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                       .append("id", id)
                       .append("channelId", channelId)
                       .append("fresh", fresh)
                       .append("link", link)
                       .append("publicationDate", publicationDate)
                       .append("title", title)
                       .append("contents", contents)
                       .append("authors", authors)
                       .toString();
    }
}