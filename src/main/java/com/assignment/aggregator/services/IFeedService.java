package com.assignment.aggregator.services;

import com.rometools.rome.feed.synd.SyndEntry;

import java.util.List;

/**
 * Interface for the feed service
 * <p>
 * Feeds are the output of valid Web syndication sources (aka {@link com.assignment.aggregator.models.Channel}). This
 * interface provides ways to fetch the feed contents
 */
public interface IFeedService
{

    /**
     * Fetch a {@link com.assignment.aggregator.models.Channel}'s feed content
     *
     * @param channelId    the id of the {@link com.assignment.aggregator.models.Channel} being fetch
     * @param forceRefresh if the channel must be fetch whether or not its TTL has yet expired
     * @return the list of the channel's published entries
     */
    List<SyndEntry> fetch(long channelId, boolean forceRefresh);

    /**
     * Aggregate the contents of all the subscribed {@link com.assignment.aggregator.models.Channel}'s feeds
     *
     * @param forceRefresh if the channel must be fetch whether or not its TTL has yet expired
     * @return the combines list of all published entries for all subscribed channels
     */
    List<SyndEntry> aggregate(boolean forceRefresh);
}
