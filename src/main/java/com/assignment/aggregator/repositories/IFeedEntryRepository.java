package com.assignment.aggregator.repositories;

import com.assignment.aggregator.models.FeedEntry;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IFeedEntryRepository extends MongoRepository<FeedEntry, String>
{

    /**
     * Get a list of persisted entries based on the channel they are related to.
     *
     * @param channelIds the list of channel id whose entries will be retrieved.
     * @return the list of persisted entries for the given list of channels
     */
    List<FeedEntry> findAllByChannelIdIn(List<Long> channelIds);

    /**
     * Delete all the feed entries related to a given list of channels
     *
     * @param channelIds the list of channel id whose entries will be retrieved.
     */
    @Modifying
    void deleteAllByChannelIdIn(List<Long> channelIds);

}
