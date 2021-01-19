package com.assignment.aggregator.services;

import com.assignment.aggregator.client.IFeedClient;
import com.assignment.aggregator.exceptions.ChannelNotFoundException;
import com.assignment.aggregator.exceptions.DuplicatedChannelException;
import com.assignment.aggregator.models.Channel;
import com.assignment.aggregator.repositories.IChannelRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Transactional
@Validated
public class ChannelService implements IChannelService
{
    private final IChannelRepository channelRepository;

    private final IFeedClient feedClient;

    public ChannelService(IChannelRepository channelRepository, IFeedClient feedClient)
    {
        this.channelRepository = channelRepository;
        this.feedClient = feedClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Channel> list()
    {
        return channelRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Channel get(@Positive long channelId)
    {
        return channelRepository.findById(channelId)
                                .orElseThrow(() -> new ChannelNotFoundException(channelId));
    }

    @Override
    public Channel create(@Valid Channel channel)
    {
        if (channelRepository.findOneByUrl(channel.getUrl()).isPresent())
        {
            throw new DuplicatedChannelException(channel.getUrl());
        }

        // let ROME determine if the provided URL is a valid syndication source. While fetching
        // the source the URL syntax is checked as well so there is no need for previous validation
        var feed = this.feedClient.fetch(channel.getUrl());

        // if no channel name is provided, use the feed title
        if (StringUtils.isBlank(channel.getName()))
        {
            channel.setName(feed.getTitle());
        }

        channelRepository.save(channel);

        channel.setLastRefresh(ZonedDateTime.now());

        return channel;
    }

    @Override
    public Channel update(@Positive long channelId, @Valid Channel updatedChannel)
    {
        var channel = channelRepository.findById(channelId)
                                       .orElseThrow(() -> new ChannelNotFoundException(channelId));

        // channel URL can't be modified

        if (StringUtils.isNotBlank(updatedChannel.getName()))
        {
            channel.setName(updatedChannel.getName());
        }

        channel.setTtl(updatedChannel.getTtl());

        return channelRepository.save(channel);
    }

    @Override
    public void delete(long channelId)
    {
        if (!channelRepository.existsById(channelId))
        {
            throw new ChannelNotFoundException(channelId);
        }

        channelRepository.deleteById(channelId);
    }

    @Override
    public Channel updateRefreshTime(Channel channel)
    {
        channel.setLastRefresh(ZonedDateTime.now());
        return channelRepository.save(channel);
    }
}