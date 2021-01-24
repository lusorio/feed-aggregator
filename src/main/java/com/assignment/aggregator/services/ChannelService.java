package com.assignment.aggregator.services;

import com.assignment.aggregator.client.IFeedClient;
import com.assignment.aggregator.exceptions.ChannelNotFoundException;
import com.assignment.aggregator.exceptions.DuplicatedChannelException;
import com.assignment.aggregator.models.Channel;
import com.assignment.aggregator.repositories.IChannelRepository;
import com.assignment.aggregator.repositories.IFeedEntryRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Transactional
@Validated
public class ChannelService implements IChannelService
{
    private static final Logger logger = LoggerFactory.getLogger(ChannelService.class);

    private final IChannelRepository channelRepository;

    private final IFeedEntryRepository feedEntryRepository;

    private final IFeedClient feedClient;

    public ChannelService(IChannelRepository channelRepository, IFeedClient feedClient, IFeedEntryRepository feedEntryRepository)
    {
        this.channelRepository = channelRepository;
        this.feedClient = feedClient;
        this.feedEntryRepository = feedEntryRepository;
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
            if (logger.isInfoEnabled())
            {
                logger.info(MessageFormat.format("Channel name not set. Using SyndFeed title: {0}", feed.getTitle()));
            }
        }

        channelRepository.save(channel);

        return channel;
    }

    @Override
    public Channel update(@Positive long channelId, @Valid Channel updatedChannel)
    {
        var channel = channelRepository.findById(channelId)
                                       .orElseThrow(() -> new ChannelNotFoundException(channelId));

        // channel URL can't be modified
        if (logger.isInfoEnabled())
        {
            logger.info("Updating channel. Channel URL can't be modified");
        }

        if (StringUtils.isNotBlank(updatedChannel.getName()))
        {
            channel.setName(updatedChannel.getName());
            if (logger.isInfoEnabled())
            {
                logger.info(MessageFormat.format("Updating name to : {0}", channel.getName()));
            }
        }

        channel.setTtl(updatedChannel.getTtl());
        if (logger.isInfoEnabled())
        {
            logger.info(MessageFormat.format("Updating TTL to : {0}", channel.getTtl()));
        }

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
        feedEntryRepository.deleteAllByChannelIdIn(List.of(channelId));

    }

    @Override
    public void updateRefreshTime(long channelId)
    {
        channelRepository.updateRefreshTime(channelId, ZonedDateTime.now());

        if (logger.isInfoEnabled())
        {
            logger.info(MessageFormat.format("Refreshing channel last update time TTL. Channel id: {0}", channelId));
        }
    }
}