package com.assignment.aggregator.services;

import com.assignment.aggregator.client.IFeedClient;
import com.assignment.aggregator.mappers.IMapper;
import com.assignment.aggregator.models.Channel;
import com.assignment.aggregator.models.FeedEntry;
import com.assignment.aggregator.repositories.IFeedEntryRepository;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional
public class FeedService implements IFeedService
{
    private static final Logger logger = LoggerFactory.getLogger(FeedService.class);

    private final IChannelService channelService;

    private final IFeedClient feedClient;

    private final IFeedEntryRepository feedEntryRepository;

    private final IMapper<SyndEntry, FeedEntry> mapper;

    public FeedService(IChannelService channelService, IFeedClient feedClient, IFeedEntryRepository feedEntryRepository, IMapper<SyndEntry, FeedEntry> mapper)
    {
        this.channelService = channelService;
        this.feedClient = feedClient;
        this.feedEntryRepository = feedEntryRepository;
        this.mapper = mapper;
    }

    @Override
    public Set<FeedEntry> fetch(long channelId, boolean forceRefresh)
    {
        var channel = channelService.get(channelId);

        // retrieve older feed entries stored in db
        var entries = new HashSet<>(feedEntryRepository.findAllByChannelIdIn(List.of(channelId)));

        if (logger.isInfoEnabled())
        {
            logger.info(MessageFormat.format("Retrieved {0} existing entries for channel [{1}, id: {2}]", entries.size(), channel.getName(), channel.getId()));
        }

        if (forceRefresh || isRefreshNeeded(channel.getTtl(), channel.getLastRefresh()))
        {
            var newEntries = this.feedClient.fetch(channel.getUrl())
                                            .getEntries()
                                            .stream()
                                            .map(e -> mapper.mapToDTO(e, FeedEntry.class))
                                            .map(e -> e.setChannelId(channelId))
                                            .collect(toSet());

            if (logger.isInfoEnabled())
            {
                logger.info(MessageFormat.format("Fetching channel {0}. Force refresh set to {1}. {2} fresh entries fetched", channelId, forceRefresh, newEntries.size()));
            }

            // update the channel to keep track of the TTL functionality
            channelService.updateRefreshTime(channel.getId());

            // remove previously fetched entries
            newEntries.removeAll(entries);

            // store the received entries in the FeedEntry collection
            feedEntryRepository.saveAll(newEntries);

            entries.addAll(newEntries.stream()
                                     .map(e -> e.setFresh(true))
                                     .collect(toSet()));
        }

        return entries;
    }

    @Override
    public Set<FeedEntry> aggregate(boolean forceRefresh)
    {
        var channelFutureMap = new HashMap<Channel, CompletableFuture<List<FeedEntry>>>();

        // retrieve older feed entries stored in db
        var entries = new HashSet<>(feedEntryRepository.findAll());

        // curated list of the updatable channels
        var channels = channelService.list().stream()
                                     .filter(c -> forceRefresh || isRefreshNeeded(c.getTtl(), c.getLastRefresh()))
                                     .collect(toList());

        channels.forEach(c ->
                         {
                             var channelFuture = this.fetchFeedEntriesAsync(c).thenApply(f -> this.mapFeedEntries(f, c.getId()));
                             channelFutureMap.put(c, channelFuture);
                         });

        var allFutures = CompletableFuture.allOf(channelFutureMap.values().toArray(CompletableFuture[]::new));

        // once all the futures completed, call future.join() to get the results and collect them in a list
        var allFeedFuture = allFutures.thenApply(v -> channelFutureMap.values().stream()
                                                                      .map(CompletableFuture::join)
                                                                      .collect(toList()));

        if (logger.isInfoEnabled())
        {
            logger.info(MessageFormat.format("Aggregating {0} channels. Force refresh set to {1}", channels.size(), forceRefresh));
        }

        var newEntries = new HashSet<FeedEntry>();

        // merge all new entries to minimize stream loops and db queries
        allFeedFuture.join().forEach(newEntries::addAll);

        //remove duplicates and persist delta
        newEntries.removeAll(entries);

        feedEntryRepository.saveAll(newEntries);

        entries.addAll(newEntries.stream()
                                 .map(e -> e.setFresh(true))
                                 .collect(toSet()));
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

    /**
     * Execute the mapper converting {@link SyndEntry} elements and sets the channel to the resulting DTO
     *
     * @param feed      the original object fetched from the feed
     * @param channelId the id of the channel it belongs to
     * @return the representation of the feed entry that will be returned to the client and persisten in DB
     */
    private List<FeedEntry> mapFeedEntries(SyndFeed feed, Long channelId)
    {
        return mapper.mapToDTO(feed.getEntries(), FeedEntry.class).stream()
                     .map(e -> e.setChannelId(channelId))
                     .collect(toList());
    }
}
