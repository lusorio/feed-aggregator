package com.assignment.aggregator.mappers;

import com.assignment.aggregator.dto.ChannelDTO;
import com.assignment.aggregator.models.Channel;
import org.springframework.stereotype.Component;

@Component
public class ChannelMapper extends Mapper<Channel, ChannelDTO>
{

    /**
     * {@inheritDoc}
     * <p>
     * Converts a ChannelDTO, which is used as payload for Channels' CRUD operations to an actual Channel
     * used by {@link com.assignment.aggregator.services.ChannelService}
     */
    @Override
    public Channel map(ChannelDTO dto, Class<Channel> destinationType)
    {
        var channel = new Channel();

        channel.setName(dto.getName());
        channel.setUrl(dto.getUrl());

        // defaults to 0 (refresh always)
        if (dto.getTtl() != null)
        {
            channel.setTtl(dto.getTtl());
        }
        else
        {
            channel.setTtl(0);
        }

        return channel;
    }
}
