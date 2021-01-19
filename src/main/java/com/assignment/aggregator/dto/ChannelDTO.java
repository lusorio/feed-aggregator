package com.assignment.aggregator.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * DTO for communication between {@link com.assignment.aggregator.models.Channel}
 * {@link org.springframework.stereotype.Controller} and {@link org.springframework.stereotype.Service} layers
 */
public class ChannelDTO
{
    /**
     * The name of the channel. If no name is provided on create/update, the feed title will be used instead
     */
    private String name;

    /**
     * The feed source URL. ROME library takes care of this URL being a valid feed source (RSS/ATOM)
     */
    private String url;

    /**
     * The time in seconds for which the {@link com.assignment.aggregator.models.Channel}s feed won't need to be refreshed
     */
    private Integer ttl;

    public ChannelDTO()
    {
        // empty constructor
    }

    public String getName()
    {
        return name;
    }

    public ChannelDTO setName(String name)
    {
        this.name = name;
        return this;
    }

    public String getUrl()
    {
        return url;
    }

    public ChannelDTO setUrl(String url)
    {
        this.url = url;
        return this;
    }

    public Integer getTtl()
    {
        return ttl;
    }

    public ChannelDTO setTtl(Integer ttl)
    {
        this.ttl = ttl;
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

        ChannelDTO that = (ChannelDTO) o;

        return new EqualsBuilder()
                       .append(getName(), that.getName())
                       .append(getUrl(), that.getUrl())
                       .append(getTtl(), that.getTtl())
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
                       .append("name", name)
                       .append("url", url)
                       .append("ttl", ttl)
                       .toString();
    }
}
