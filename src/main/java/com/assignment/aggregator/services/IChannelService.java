package com.assignment.aggregator.services;

import com.assignment.aggregator.models.Channel;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * Interface for the channel service
 * <p>
 * Channels are representations of web syndication feed sources. Channels must be given a unique
 * name and URL, the latest providing a valid RSS or ATOM syndication feed
 */
public interface IChannelService
{

    /**
     * List all the channels the user has subscribed to
     *
     * @return the list of channels the user has subscribed to. Returns an empty list if no subscriptions exist yet
     */
    List<Channel> list();

    /**
     * Get a channel within the list of channels the user has subscribed to based on the channel id
     *
     * @param channelId the id of the channel to fetch
     * @return the channel matching the <code>channelId</code>
     * @throws com.assignment.aggregator.exceptions.ChannelNotFoundException if the channel is not found
     */
    Channel get(@Positive long channelId);

    /**
     * Create a new channel subscription
     *
     * @param channel the channel to subscribe to
     * @return the subscribed channel
     * @throws com.assignment.aggregator.exceptions.DuplicatedChannelException if the channel has already been subscribed
     */
    Channel create(@Valid Channel channel);

    /**
     * Update a subscribed channel
     *
     * @param channelId the id of the channel to update
     * @param channel   the new representation of the channel
     * @return the updated channel
     * @throws com.assignment.aggregator.exceptions.ChannelNotFoundException if the channel is not found
     */
    Channel update(@Positive long channelId, @Valid Channel channel);

    /**
     * Delete a channel from the subscription list
     *
     * @param channelId the id of the channel to delete
     * @throws com.assignment.aggregator.exceptions.ChannelNotFoundException if the channel is not found
     */
    void delete(long channelId);

    /**
     * Updates a {@link Channel} last refresh time
     *
     * @param channelId the id of the {@link Channel} to update
     */
    void updateRefreshTime(long channelId);
}
