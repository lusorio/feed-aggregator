package com.assignment.aggregator.models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

/**
 * Represents a subscribed channel (Feed source)
 */
@Entity
@Table(name = "channel")
public class Channel
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @Size(min = 2, max = 255)
    private String name;

    @Column(name = "url", nullable = false, unique = true)
    @Size(max = 2048)
    @NotNull
    private String url;

    @Column(name = "ttl", nullable = false)
    @PositiveOrZero
    @NotNull
    private Integer ttl;

    @Column(name = "last_refresh")
    private ZonedDateTime lastRefresh;

    @Column(name = "created", nullable = false)
    private ZonedDateTime created;

    @Column(name = "updated", nullable = false)
    private ZonedDateTime updated;

    public Channel()
    {
        // empty constructor
    }

    public Channel(String name, String url, Integer ttl)
    {
        this.name = name;
        this.url = url;
        this.ttl = ttl;
    }

    public Long getId()
    {
        return id;
    }

    public Channel setId(Long id)
    {
        this.id = id;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public Channel setName(String name)
    {
        this.name = name;
        return this;
    }

    public String getUrl()
    {
        return url;
    }

    public Channel setUrl(String url)
    {
        this.url = url;
        return this;
    }

    public Integer getTtl()
    {
        return ttl;
    }

    public Channel setTtl(Integer ttl)
    {
        this.ttl = ttl;
        return this;
    }

    public ZonedDateTime getLastRefresh()
    {
        return lastRefresh;
    }

    public Channel setLastRefresh(ZonedDateTime lastRefresh)
    {
        this.lastRefresh = lastRefresh;
        return this;
    }

    public ZonedDateTime getCreated()
    {
        return created;
    }

    public Channel setCreated(ZonedDateTime created)
    {
        this.created = created;
        return this;
    }

    public ZonedDateTime getUpdated()
    {
        return updated;
    }

    public Channel setUpdated(ZonedDateTime updated)
    {
        this.updated = updated;
        return this;
    }

    @PrePersist
    protected void onCreate()
    {
        this.updated = this.created = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updated = ZonedDateTime.now();
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

        Channel channel = (Channel) o;

        return new EqualsBuilder()
                       .append(getName(), channel.getName())
                       .append(getUrl(), channel.getUrl())
                       .append(getTtl(), channel.getTtl())
                       .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                       .append(getName())
                       .append(getUrl())
                       .append(getTtl())
                       .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                       .append("id", id)
                       .append("name", name)
                       .append("url", url)
                       .append("ttl", ttl)
                       .append("lastRefresh", lastRefresh)
                       .append("created", created)
                       .append("updated", updated)
                       .toString();
    }
}
