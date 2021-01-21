package com.assignment.aggregator.services;

import com.assignment.aggregator.client.IFeedClient;
import com.assignment.aggregator.models.Channel;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeedService implements IFeedService
{
    private static final Logger logger = LoggerFactory.getLogger(FeedService.class);

    private final IChannelService channelService;

    private final IFeedClient feedClient;

    public FeedService(IChannelService channelService, IFeedClient feedClient)
    {
        this.channelService = channelService;
        this.feedClient = feedClient;
    }

    @Override
    public List<SyndEntry> fetch(long channelId, boolean forceRefresh)
    {
        var channel = channelService.get(channelId);

        if (forceRefresh || isRefreshNeeded(channel.getTtl(), channel.getLastRefresh()))
        {
            var entries = this.feedClient.fetch(channel.getUrl())
                                         .getEntries();

            if (logger.isInfoEnabled())
            {
                logger.info(MessageFormat.format("Fetching channel {0}. Force refresh set to {1}", channelId, forceRefresh));
            }

            channelService.updateRefreshTime(channel.getId());

            return entries;
        }

        return List.of();
    }

    @Override
    public List<SyndEntry> aggregate(boolean forceRefresh)
    {
        var entries = new ArrayList<SyndEntry>();

        var feedFutures = channelService.list().stream()
                                        .filter(c -> forceRefresh || isRefreshNeeded(c.getTtl(), c.getLastRefresh()))
                                        .map(this::fetchFeedEntriesAsync)
                                        .collect(Collectors.toList());

        var allFutures = CompletableFuture.allOf(feedFutures.toArray(CompletableFuture[]::new));

        // once all the futures completed, call future.join() to get the results and collect them in a list
        var allFeedFuture = allFutures.thenApply(v -> feedFutures.stream()
                                                                 .map(CompletableFuture::join)
                                                                 .collect(Collectors.toList()));

        if (logger.isInfoEnabled())
        {
            logger.info(MessageFormat.format("Aggregating {0} channels. Force refresh set to {1}", feedFutures.size(), forceRefresh));
        }

        allFeedFuture.join().forEach(f -> entries.addAll(f.getEntries()));

        return entries;
    }

    /**
     * Asynchronously fetch of a {@link Channel}'s feed
     *
     * @param channel the {@link Channel} to be fetch
     * @return the {@link CompletableFuture} holding the fetching operation execution
     */
    @Async("asyncExecutor")
    CompletableFuture<SyndFeed> fetchFeedEntriesAsync(Channel channel)
    {
        channelService.updateRefreshTime(channel.getId());
        return CompletableFuture.supplyAsync(() -> this.feedClient.fetch(channel.getUrl()));
    }

    /**
     * Determine if the channels TTL has expired and thus the feed must be fetched
     *
     * @param ttl             the channel's TTL
     * @param lastRefreshedOn the date of the latest channel's refresh
     * @return <code>true</code> if the channel's TTL has expired or <code>false</code> otherwise
     */
    private boolean isRefreshNeeded(Integer ttl, ZonedDateTime lastRefreshedOn)
    {
        if (ttl == null || lastRefreshedOn == null)
        {
            return true;
        }

        var timeElapsed = Duration.between(lastRefreshedOn.toInstant(), Instant.now());

        return timeElapsed.getSeconds() > ttl;
    }
}
