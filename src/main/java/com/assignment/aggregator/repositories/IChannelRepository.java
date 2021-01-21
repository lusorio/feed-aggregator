package com.assignment.aggregator.repositories;

import com.assignment.aggregator.models.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;

@Repository
public interface IChannelRepository extends JpaRepository<Channel, Long>
{
    /**
     * Fetch a channel entity by its source URL. Provides an easy way to (business) validate that no duplicate entries
     * are created.
     *
     * @param url the url of the channel being created
     * @return an {@link Optional} of the channel
     */
    Optional<Channel> findOneByUrl(String url);

    /**
     * Updates the last refresh date for a channel
     *
     * @param channelId the id of the channel to update
     * @param when      the {@link ZonedDateTime} value to set
     */
    @Modifying
    @Query("update Channel ch set ch.lastRefresh = ?2 where ch.id = ?1")
    void updateRefreshTime(long channelId, ZonedDateTime when);
}
