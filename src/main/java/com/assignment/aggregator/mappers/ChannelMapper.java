package com.assignment.aggregator.mappers;

import com.assignment.aggregator.dto.ChannelDTO;
import com.assignment.aggregator.models.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public class ChannelMapper extends Mapper<Channel, ChannelDTO>
{

    private static final Logger logger = LoggerFactory.getLogger(ChannelMapper.class);

    /**
     * {@inheritDoc}
     * <p>
     * Converts a ChannelDTO, which is used as payload for Channels' CRUD operations to an actual Channel
     * used by {@link com.assignment.aggregator.services.ChannelService}
     */
    @Override
    public Channel map(ChannelDTO dto, Class<Channel> destinationType)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Mapping ChannelDTO to Channel entity");
        }

        var channel = new Channel();

        channel.setName(dto.getName());
        channel.setUrl(dto.getUrl());
        if (logger.isInfoEnabled())
        {
            logger.info(MessageFormat.format("Channel name set to: {0}", channel.getName()));
            logger.info(MessageFormat.format("Channel URL set to: {0}", channel.getUrl()));
        }

        // defaults to 0 (refresh always)
        if (dto.getTtl() != null)
        {
            channel.setTtl(dto.getTtl());
        }
        else
        {
            channel.setTtl(0);
            if (logger.isInfoEnabled())
            {
                logger.info("Channel TTL is missing. Default to 0 (always refresh)");
            }
        }

        return channel;
    }
}
