package com.assignment.aggregator.services;

import com.assignment.aggregator.AbstractSpringTest;
import com.assignment.aggregator.client.IFeedClient;
import com.assignment.aggregator.exceptions.ChannelNotFoundException;
import com.assignment.aggregator.mappers.IMapper;
import com.assignment.aggregator.models.Channel;
import com.assignment.aggregator.models.FeedEntry;
import com.assignment.aggregator.repositories.IFeedEntryRepository;
import com.rometools.rome.feed.synd.SyndEntry;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedServiceTest extends AbstractSpringTest
{
    @Mock
    private IChannelService channelService;

    @Mock
    private IFeedClient feedClient;

    @Mock
    private IFeedEntryRepository feedEntryRepository;

    @Mock
    private IMapper<SyndEntry, FeedEntry> mapper;

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
            verifyNoInteractions(feedEntryRepository);
            verifyNoInteractions(mapper);
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

            verify(feedEntryRepository, times(1)).findAllByChannelIdIn(List.of(channel.getId()));
            verifyNoMoreInteractions(feedEntryRepository);

            verifyNoInteractions(feedClient);
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("Refresh should take place if forced, even if the channel's TTL hasn't yet expired")
        void fetch_TTLNotExpired_ForcedRefresh()
        {
            var channel = new Channel("name", "url", 3600);
            channel.setId(1L);

            // channel's TTL hasn't yet expired
            channel.setLastRefresh(ZonedDateTime.of(LocalDateTime.now().minusSeconds(1000), ZoneId.systemDefault()));

            var entry1 = new SyndEntryImpl();
            entry1.setLink("link_1");

            var entry2 = new SyndEntryImpl();
            entry2.setLink("link_2");

            var dto1 = new FeedEntry();
            dto1.setLink("link_1");

            var dto2 = new FeedEntry();
            dto2.setLink("link_2");

            var feed = new SyndFeedImpl();
            feed.getEntries().addAll(List.of(entry1, entry2));

            when(channelService.get(channel.getId())).thenReturn(channel);
            when(feedEntryRepository.findAllByChannelIdIn(List.of(channel.getId()))).thenReturn(List.of());
            when(feedClient.fetch(channel.getUrl())).thenReturn(feed);
            when(mapper.mapToDTO(any(SyndEntryImpl.class), eq(FeedEntry.class))).thenReturn(dto1)
                                                                                .thenReturn(dto2);
            // channel is forced to refresh
            var result = service.fetch(channel.getId(), true);

            assertFalse(result.isEmpty());

            verify(channelService, times(1)).get(channel.getId());
            verify(channelService, times(1)).updateRefreshTime(channel.getId());
            verifyNoMoreInteractions(channelService);

            verify(feedEntryRepository, times(1)).findAllByChannelIdIn(List.of(channel.getId()));
            verify(feedEntryRepository, times(1)).saveAll(Set.of(dto1, dto2));
            verifyNoMoreInteractions(feedEntryRepository);

            verify(feedClient, times(1)).fetch(channel.getUrl());
            verifyNoMoreInteractions(feedClient);

            verify(mapper, times(1)).mapToDTO(entry1, FeedEntry.class);
            verify(mapper, times(1)).mapToDTO(entry2, FeedEntry.class);
            verifyNoMoreInteractions(mapper);
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

            var entry1 = new SyndEntryImpl();
            entry1.setLink("link_1");

            var entry2 = new SyndEntryImpl();
            entry2.setLink("link_2");

            var dto1 = new FeedEntry();
            dto1.setLink("link_1");

            var dto2 = new FeedEntry();
            dto2.setLink("link_2");

            var feed = new SyndFeedImpl();
            feed.getEntries().addAll(List.of(entry1, entry2));

            when(channelService.get(channel.getId())).thenReturn(channel);
            when(feedClient.fetch(channel.getUrl())).thenReturn(feed);
            when(mapper.mapToDTO(any(SyndEntryImpl.class), eq(FeedEntry.class))).thenReturn(dto1)
                                                                                .thenReturn(dto2);

            var result = service.fetch(channel.getId(), forceRefresh);

            assertFalse(result.isEmpty());

            verify(channelService, times(1)).get(channel.getId());
            verify(channelService, times(1)).updateRefreshTime(channel.getId());
            verifyNoMoreInteractions(channelService);

            verify(feedClient, times(1)).fetch(channel.getUrl());
            verifyNoMoreInteractions(feedClient);

            verify(feedEntryRepository, times(1)).findAllByChannelIdIn(List.of(channel.getId()));
            verify(feedEntryRepository, times(1)).saveAll(Set.of(dto1, dto2));
            verifyNoMoreInteractions(feedEntryRepository);

            verify(mapper, times(1)).mapToDTO(entry1, FeedEntry.class);
            verify(mapper, times(1)).mapToDTO(entry2, FeedEntry.class);
            verifyNoMoreInteractions(mapper);
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
            when(mapper.mapToDTO(any(SyndEntryImpl.class), eq(FeedEntry.class))).thenReturn(new FeedEntry())
                                                                                .thenReturn(new FeedEntry());

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

            var channel1Entry1 = new FeedEntry();
            channel1Entry1.setChannelId(channel1.getId());
            channel1Entry1.setLink("url11");

            var channel1Entry2 = new FeedEntry();
            channel1Entry2.setChannelId(channel1.getId());
            channel1Entry2.setLink("url12");

            var channel2 = new Channel("channel 2", "url2", 3600);
            channel2.setId(2L);
            channel2.setLastRefresh(ZonedDateTime.now());

            var channel2Entry1 = new FeedEntry();
            channel2Entry1.setChannelId(channel2.getId());
            channel2Entry1.setLink("url21");

            var channel2Entry2 = new FeedEntry();
            channel2Entry2.setChannelId(channel2.getId());
            channel2Entry2.setLink("url22");

            var feed = new SyndFeedImpl();
            feed.getEntries().addAll(List.of(new SyndEntryImpl(), new SyndEntryImpl()));

            // each channel's first entry has already been fetched
            when(feedEntryRepository.findAll()).thenReturn(List.of(channel1Entry1, channel2Entry1));

            when(channelService.list()).thenReturn(List.of(channel1, channel2));

            when(feedClient.fetch(anyString())).thenReturn(feed);

            // assume the mapper maps all the entries (as the four of them have been fetched from source)
            when(mapper.mapToDTO(any(SyndEntryImpl.class), eq(FeedEntry.class))).thenReturn(channel1Entry1)
                                                                                .thenReturn(channel1Entry2)
                                                                                .thenReturn(channel2Entry1)
                                                                                .thenReturn(channel2Entry2);

            service.aggregate(true);

            verify(feedEntryRepository, times(1)).findAll();
            verify(feedEntryRepository, times(1)).saveAll(anySet());
            verifyNoMoreInteractions(feedEntryRepository);

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