package com.assignment.aggregator.services;

import com.assignment.aggregator.models.FeedEntry;

import java.util.Collection;

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
     * <p>
     * This operation will return a combined list of the entries previously retrieved from the feed and stored
     * in the application's DB and the result of a new fetch operation to the channel's feed.
     * If <code>forceRefresh</code> is set to <code>false</code> and the channel's TTL has not yet expired,
     * only the pre-existing entries will return.
     *
     * @param channelId    the id of the {@link com.assignment.aggregator.models.Channel} being fetch
     * @param forceRefresh if the channel must be fetch whether or not its TTL has yet expired
     * @return the list of the channel's published entries
     */
    Collection<FeedEntry> fetch(long channelId, boolean forceRefresh);

    /**
     * Aggregate the contents of all the subscribed {@link com.assignment.aggregator.models.Channel}'s feeds
     * <p>
     * This operation is the multi-channel equivalent of {@link IFeedService#fetch}. Persisted entries for all
     * subscribed channels will be returned, however, only channels eligible for update will have a fresh version
     * of the feed.
     *
     * @param forceRefresh if the channels must be fetch whether or not their TTL has yet expired
     * @return the combined list of all published entries for all subscribed channels
     */
    Collection<FeedEntry> aggregate(boolean forceRefresh);
}
