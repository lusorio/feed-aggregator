package com.assignment.aggregator.services;

import com.assignment.aggregator.AbstractSpringTest;
import com.assignment.aggregator.client.IFeedClient;
import com.assignment.aggregator.exceptions.ChannelNotFoundException;
import com.assignment.aggregator.models.Channel;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedServiceTest extends AbstractSpringTest
{
    @Mock
    private IChannelService channelService;

    @Mock
    private IFeedClient feedClient;

    @InjectMocks
    private FeedService service;

    @Nested
    @DisplayName("Test the fetch() method")
    class Fetch
    {
        @Test
        @DisplayName("A ChannelNotFoundException must be thrown if the channel is not found")
        void fetch_ChannelNotFound()
        {
            var channelId = 1L;
            when(channelService.get(channelId)).thenThrow(ChannelNotFoundException.class);

            assertAll(
                    () -> assertThrows(ChannelNotFoundException.class, () -> service.fetch(channelId, false)),
                    () -> assertThrows(ChannelNotFoundException.class, () -> service.fetch(channelId, true)));

            verify(channelService, times(2)).get(channelId);
            verifyNoMoreInteractions(channelService);

            verifyNoInteractions(feedClient);
        }

        @Test
        @DisplayName("No refresh should take place if the channel's TTL hasn't expired and the refresh is not forced")
        void fetch_TTLNotExpired_NotForcedRefresh()
        {
            var channel = new Channel("name", "url", 3600);
            channel.setId(1L);

            // channel's TTL hasn't yet expired
            channel.setLastRefresh(ZonedDateTime.of(LocalDateTime.now().minusSeconds(1000), ZoneId.systemDefault()));

            when(channelService.get(channel.getId())).thenReturn(channel);

            // channel is not forced to refresh
            var result = service.fetch(channel.getId(), false);

            assertTrue(result.isEmpty());

            verify(channelService, times(1)).get(channel.getId());
            verifyNoMoreInteractions(channelService);

            verifyNoInteractions(feedClient);
        }

        @Test
        @DisplayName("Refresh should take place if forced, even if the channel's TTL hasn't yet expired")
        void fetch_TTLNotExpired_ForcedRefresh()
        {
            var channel = new Channel("name", "url", 3600);
            channel.setId(1L);

            // channel's TTL hasn't yet expired
            channel.setLastRefresh(ZonedDateTime.of(LocalDateTime.now().minusSeconds(1000), ZoneId.systemDefault()));

            var feed = new SyndFeedImpl();
            feed.getEntries().add(new SyndEntryImpl());

            when(channelService.get(channel.getId())).thenReturn(channel);
            when(feedClient.fetch(channel.getUrl())).thenReturn(feed);

            // channel is forced to refresh
            var result = service.fetch(channel.getId(), true);

            assertFalse(result.isEmpty());

            verify(channelService, times(1)).get(channel.getId());
            verify(channelService, times(1)).updateRefreshTime(channel.getId());
            verifyNoMoreInteractions(channelService);

            verify(feedClient, times(1)).fetch(channel.getUrl());
            verifyNoMoreInteractions(feedClient);
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        @DisplayName("Refresh should take place if the channel's TTL has expired, whether or not is forced to refresh")
        void fetch_TTLExpired(boolean forceRefresh)
        {
            var channel = new Channel("name", "url", 3600);
            channel.setId(1L);

            // channel's TTL has expired by 1 sec (plus exec time)
            channel.setLastRefresh(ZonedDateTime.of(LocalDateTime.now().minusSeconds(3601), ZoneId.systemDefault()));

            var feed = new SyndFeedImpl();
            feed.getEntries().add(new SyndEntryImpl());

            when(channelService.get(channel.getId())).thenReturn(channel);
            when(feedClient.fetch(channel.getUrl())).thenReturn(feed);

            var result = service.fetch(channel.getId(), forceRefresh);

            assertFalse(result.isEmpty());

            verify(channelService, times(1)).get(channel.getId());
            verify(channelService, times(1)).updateRefreshTime(channel.getId());
            verifyNoMoreInteractions(channelService);

            verify(feedClient, times(1)).fetch(channel.getUrl());
            verifyNoMoreInteractions(feedClient);
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        @DisplayName("Refresh should take place if the channel's TTL has expired, whether or not is forced to refresh")
        void fetch_NullTTLForcesRefresh(boolean forceRefresh)
        {
            // channel has no explicit TTL
            var channel = new Channel("name", "url", null);
            channel.setId(1L);

            // channel's TTL has expired by 1 sec (plus exec time)
            channel.setLastRefresh(ZonedDateTime.of(LocalDateTime.now().minusSeconds(1000), ZoneId.systemDefault()));

            var feed = new SyndFeedImpl();
            feed.getEntries().add(new SyndEntryImpl());

            when(channelService.get(channel.getId())).thenReturn(channel);
            when(feedClient.fetch(channel.getUrl())).thenReturn(feed);

            var result = service.fetch(channel.getId(), forceRefresh);

            assertFalse(result.isEmpty());

            verify(channelService, times(1)).get(channel.getId());
            verify(channelService, times(1)).updateRefreshTime(channel.getId());
            verifyNoMoreInteractions(channelService);

            verify(feedClient, times(1)).fetch(channel.getUrl());
            verifyNoMoreInteractions(feedClient);
        }
    }

    @Nested
    @DisplayName("Test the aggregate() method")
    class Aggregate
    {
        @Test
        @DisplayName("An empty list should be returned when calling aggregate() if no channels are subscribed")
        void aggregate_NoChannels()
        {
            when(channelService.list()).thenReturn(List.of());

            service.aggregate(true);

            verify(channelService, times(1)).list();
            verifyNoMoreInteractions(channelService);

            verifyNoInteractions(feedClient);
        }

        @Test
        @DisplayName("No fetch should be performed on a channel if its TTL has not yet expired and is not being forced to refresh")
        void aggregate_UnexpiredTTL()
        {
            var channel1 = new Channel("channel 1", "url", 3600);
            channel1.setId(1L);
            channel1.setLastRefresh(ZonedDateTime.now());

            var channel2 = new Channel("channel 2", "url", 3600);
            channel2.setId(2L);
            channel2.setLastRefresh(ZonedDateTime.now());

            when(channelService.list()).thenReturn(List.of(channel1, channel2));

            service.aggregate(false);

            verify(channelService, times(1)).list();
            verifyNoMoreInteractions(channelService);

            // neither channel is fetched as both have unexpired TTLs
            verifyNoInteractions(feedClient);
        }

        @Test
        @DisplayName("A channel must be fetched if forced even if its TTL has not yet expired")
        void aggregate_UnexpiredTTLWithForceRefresh()
        {
            var channel1 = new Channel("channel 1", "url", 3600);
            channel1.setId(1L);
            channel1.setLastRefresh(ZonedDateTime.now());

            var channel2 = new Channel("channel 2", "url2", 3600);
            channel2.setId(2L);
            channel2.setLastRefresh(ZonedDateTime.now());

            var feed = new SyndFeedImpl();
            feed.getEntries().addAll(List.of(new SyndEntryImpl(), new SyndEntryImpl()));

            when(channelService.list()).thenReturn(List.of(channel1, channel2));
            when(feedClient.fetch(anyString())).thenReturn(feed);

            var result = service.aggregate(true);

            assertEquals(4, result.size());

            verify(channelService, times(1)).list();
            verify(channelService, times(1)).updateRefreshTime(channel1.getId());
            verify(channelService, times(1)).updateRefreshTime(channel2.getId());
            verifyNoMoreInteractions(channelService);

            // neither channel is fetched as both have unexpired TTLs
            verify(feedClient, times(1)).fetch(channel1.getUrl());
            verify(feedClient, times(1)).fetch(channel2.getUrl());
            verifyNoMoreInteractions(feedClient);
        }
    }
}