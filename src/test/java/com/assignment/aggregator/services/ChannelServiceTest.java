package com.assignment.aggregator.services;

import com.assignment.aggregator.AbstractSpringTest;
import com.assignment.aggregator.client.IFeedClient;
import com.assignment.aggregator.exceptions.ChannelNotFoundException;
import com.assignment.aggregator.exceptions.DuplicatedChannelException;
import com.assignment.aggregator.exceptions.InvalidChannelException;
import com.assignment.aggregator.models.Channel;
import com.assignment.aggregator.repositories.IChannelRepository;
import com.assignment.aggregator.repositories.IFeedEntryRepository;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChannelServiceTest extends AbstractSpringTest
{

    @Mock
    private IFeedClient feedClient;

    @Mock
    private IChannelRepository channelRepository;

    @Mock
    private IFeedEntryRepository feedEntryRepository;

    @InjectMocks
    private ChannelService service;

    @Nested
    @DisplayName("Test the list() method")
    class List
    {

        @Test
        @DisplayName("When there are no channel subscriptions, the service must return an empty list")
        void list_ReturnsEmptyList()
        {
            when(channelRepository.findAll()).thenReturn(java.util.List.of());

            var result = service.list();

            assertTrue(result.isEmpty());

            verify(channelRepository, times(1)).findAll();
            verifyNoMoreInteractions(channelRepository);

            verifyNoInteractions(feedClient);
            verifyNoInteractions(feedEntryRepository);
        }

        @Test
        @DisplayName("The service must return the list of subscriptions returned by the repository")
        void list()
        {
            var size = 45;

            var channels = new ArrayList<Channel>(size);

            for (var i = 0; i < size; i++)
            {
                channels.add(new Channel("name", "url" + i, 0));
            }

            when(channelRepository.findAll()).thenReturn(channels);

            assertEquals(channels, service.list());

            verify(channelRepository, times(1)).findAll();
            verifyNoMoreInteractions(channelRepository);

            verifyNoInteractions(feedClient);
            verifyNoInteractions(feedEntryRepository);
        }
    }

    @Nested
    @DisplayName("Test the get() method")
    class Get
    {
        @Test
        @DisplayName("A ChannelNotFoundException exception must be thrown if the channel id is unknown")
        void get_ThrowsException_ChannelNotFound()
        {
            when(channelRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(ChannelNotFoundException.class, () -> service.get(anyLong()));

            verify(channelRepository, times(1)).findById(anyLong());
            verifyNoMoreInteractions(channelRepository);

            verifyNoInteractions(feedClient);
            verifyNoInteractions(feedEntryRepository);
        }

        @Test
        @DisplayName("The service must return the matching channel")
        void get()
        {
            var channel = new Channel("name", "url", 0);

            when(channelRepository.findById(anyLong())).thenReturn(Optional.of(channel));

            assertEquals(channel, service.get(anyLong()));

            verify(channelRepository, times(1)).findById(anyLong());
            verifyNoMoreInteractions(channelRepository);

            verifyNoInteractions(feedClient);
            verifyNoInteractions(feedEntryRepository);
        }
    }

    @Nested
    @DisplayName("Test the create() method")
    class Create
    {

        @Test
        @DisplayName("A DuplicatedChannelException exception must be thrown if the channel is already subscribed")
        void create_ThrowsException_DuplicatedChannelException()
        {
            var channel = new Channel("name", "url", 0);
            when(channelRepository.findOneByUrl(anyString())).thenReturn(Optional.of(channel));

            assertThrows(DuplicatedChannelException.class, () -> service.create(channel));

            verify(channelRepository, times(1)).findOneByUrl(anyString());
            verifyNoMoreInteractions(channelRepository);

            verifyNoInteractions(feedClient);
            verifyNoInteractions(feedEntryRepository);
        }

        @Test
        @DisplayName("A InvalidChannelException exception must be thrown if the feed client can't read the source")
        void create_ThrowsException_InvalidChannelException()
        {
            var channel = new Channel("name", "url", 0);

            when(channelRepository.findOneByUrl(anyString())).thenReturn(Optional.empty());
            when(feedClient.fetch(anyString())).thenThrow(InvalidChannelException.class);

            assertThrows(InvalidChannelException.class, () -> service.create(channel));

            verify(channelRepository, times(1)).findOneByUrl(anyString());
            verifyNoMoreInteractions(channelRepository);

            verify(feedClient, times(1)).fetch(anyString());
            verifyNoMoreInteractions(channelRepository);

            verifyNoInteractions(feedEntryRepository);
        }

        @Test
        @DisplayName("If the optional channel parameters are not set, defaults are used")
        void create_ChannelNameNotProvided()
        {
            var feed = new SyndFeedImpl();
            feed.setTitle("Feed Title");
            feed.setPublishedDate(new Date());

            var channel = new Channel(null, "url", 0);
            var completedChannel = new Channel(feed.getTitle(), channel.getUrl(), 0);

            when(channelRepository.findOneByUrl(anyString())).thenReturn(Optional.empty());
            when(feedClient.fetch(anyString())).thenReturn(feed);

            var result = service.create(channel);

            assertEquals(completedChannel, result);

            verify(channelRepository, times(1)).findOneByUrl(anyString());
            verify(channelRepository, times(1)).save(completedChannel);
            verifyNoMoreInteractions(channelRepository);

            verify(feedClient, times(1)).fetch(anyString());
            verifyNoMoreInteractions(channelRepository);

            verifyNoInteractions(feedEntryRepository);
        }
    }

    @Nested
    @DisplayName("Test the update() method")
    class Update
    {
        @Test
        @DisplayName("A ChannelNotFoundException exception must be thrown if the channel is not subscribed")
        void update_ThrowsException_ChannelNotFoundException()
        {
            var channel = new Channel("name", "url", 0);
            channel.setId(1L);

            when(channelRepository.findById(channel.getId())).thenReturn(Optional.empty());

            assertThrows(ChannelNotFoundException.class, () -> service.update(channel.getId(), channel));

            verify(channelRepository, times(1)).findById(channel.getId());
            verifyNoMoreInteractions(channelRepository);

            verifyNoInteractions(feedClient);
            verifyNoInteractions(feedEntryRepository);
        }

        @Test
        @DisplayName("Updated takes place")
        void update()
        {
            var channel = new Channel("name", "url", 0);
            channel.setId(1L);

            when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

            service.update(channel.getId(), channel);

            verify(channelRepository, times(1)).findById(channel.getId());
            verify(channelRepository, times(1)).save(any());
            verifyNoMoreInteractions(channelRepository);

            verifyNoInteractions(feedClient);
            verifyNoInteractions(feedEntryRepository);
        }
    }

    @Nested
    @DisplayName("Test the delete() method")
    class Delete
    {
        @Test
        @DisplayName("A ChannelNotFoundException exception must be thrown if the channel is not subscribed")
        void delete_ThrowsException_ChannelNotFoundException()
        {
            var channelId = 1L;

            when(channelRepository.existsById(channelId)).thenReturn(false);

            assertThrows(ChannelNotFoundException.class, () -> service.delete(channelId));

            verify(channelRepository, times(1)).existsById(channelId);
            verifyNoMoreInteractions(channelRepository);

            verifyNoInteractions(feedClient);
            verifyNoInteractions(feedEntryRepository);
        }

        @Test
        @DisplayName("Delete takes place")
        void delete()
        {
            var channelId = 1L;

            when(channelRepository.existsById(channelId)).thenReturn(true);

            service.delete(channelId);

            verify(channelRepository, times(1)).existsById(channelId);
            verify(channelRepository, times(1)).deleteById(channelId);
            verifyNoMoreInteractions(channelRepository);

            verify(feedEntryRepository, times(1)).deleteAllByChannelIdIn(java.util.List.of(channelId));

            verifyNoInteractions(feedClient);
        }
    }
}