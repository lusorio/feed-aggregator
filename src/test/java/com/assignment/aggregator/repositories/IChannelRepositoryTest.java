package com.assignment.aggregator.repositories;

import com.assignment.aggregator.models.Channel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@DataJpaTest
class IChannelRepositoryTest
{

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IChannelRepository repository;

    @Nested
    class FindOneByUrl
    {
        @ParameterizedTest
        @ValueSource(strings = {"unknown.url"})
        @NullAndEmptySource
        void findOneByUrl_UnknownURL(String url)
        {
            Assertions.assertTrue(repository.findOneByUrl(url).isEmpty());
        }

        @Test
        void findOneByUrl()
        {
            var channel = new Channel("channelName", "channelURL", 1);
            entityManager.persistAndFlush(channel);

            Assertions.assertEquals(channel, repository.findOneByUrl(channel.getUrl()).orElse(null));
        }
    }

    @Nested
    class UpdateRefreshTime
    {
        @Test
        void updateRefreshTime()
        {
            var originalDate = ZonedDateTime.of(LocalDateTime.MIN, ZoneId.systemDefault());

            var channel = new Channel("channelName", "channelURL", 1);
            channel.setLastRefresh(originalDate);

            entityManager.persistAndFlush(channel);

            repository.updateRefreshTime(channel.getId(), ZonedDateTime.now());

            Assertions.assertTrue(entityManager.refresh(channel).getLastRefresh().isAfter(originalDate));
        }
    }
}