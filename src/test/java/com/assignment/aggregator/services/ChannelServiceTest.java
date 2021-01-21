package com.assignment.aggregator.services;

import com.assignment.aggregator.AbstractSpringTest;
import com.assignment.aggregator.client.IFeedClient;
import com.assignment.aggregator.exceptions.ChannelNotFoundException;
import com.assignment.aggregator.exceptions.DuplicatedChannelException;
import com.assignment.aggregator.exceptions.InvalidChannelException;
import com.assignment.aggregator.models.Channel;
import com.assignment.aggregator.repositories.IChannelRepository;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.ZonedDateTime;
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
    private IChannelRepository repository;

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
            when(repository.findAll()).thenReturn(java.util.List.of());

            var result = service.list();

            assertTrue(result.isEmpty());

            verify(repository, times(1)).findAll();
            verifyNoMoreInteractions(repository);

            verifyNoInteractions(feedClient);
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

            when(repository.findAll()).thenReturn(channels);

            assertEquals(channels, service.list());

            verify(repository, times(1)).findAll();
            verifyNoMoreInteractions(repository);

            verifyNoInteractions(feedClient);
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
            when(repository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(ChannelNotFoundException.class, () -> service.get(anyLong()));

            verify(repository, times(1)).findById(anyLong());
            verifyNoMoreInteractions(repository);

            verifyNoInteractions(feedClient);
        }

        @Test
        @DisplayName("The service must return the matching channel")
        void get()
        {
            var channel = new Channel("name", "url", 0);

            when(repository.findById(anyLong())).thenReturn(Optional.of(channel));

            assertEquals(channel, service.get(anyLong()));

            verify(repository, times(1)).findById(anyLong());
            verifyNoMoreInteractions(repository);

            verifyNoInteractions(feedClient);
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
            when(repository.findOneByUrl(anyString())).thenReturn(Optional.of(channel));

            assertThrows(DuplicatedChannelException.class, () -> service.create(channel));

            verify(repository, times(1)).findOneByUrl(anyString());
            verifyNoMoreInteractions(repository);

            verifyNoInteractions(feedClient);
        }

        @Test
        @DisplayName("A InvalidChannelException exception must be thrown if the feed client can't read the source")
        void create_ThrowsException_InvalidChannelException()
        {
            var channel = new Channel("name", "url", 0);

            when(repository.findOneByUrl(anyString())).thenReturn(Optional.empty());
            when(feedClient.fetch(anyString())).thenThrow(InvalidChannelException.class);

            assertThrows(InvalidChannelException.class, () -> service.create(channel));

            verify(repository, times(1)).findOneByUrl(anyString());
            verifyNoMoreInteractions(repository);

            verify(feedClient, times(1)).fetch(anyString());
            verifyNoMoreInteractions(repository);
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

            when(repository.findOneByUrl(anyString())).thenReturn(Optional.empty());
            when(feedClient.fetch(anyString())).thenReturn(feed);

            var result = service.create(channel);

            assertEquals(completedChannel, result);

            verify(repository, times(1)).findOneByUrl(anyString());
            verify(repository, times(1)).save(completedChannel);
            verifyNoMoreInteractions(repository);

            verify(feedClient, times(1)).fetch(anyString());
            verifyNoMoreInteractions(repository);
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

            when(repository.findById(channel.getId())).thenReturn(Optional.empty());

            assertThrows(ChannelNotFoundException.class, () -> service.update(channel.getId(), channel));

            verify(repository, times(1)).findById(channel.getId());
            verifyNoMoreInteractions(repository);

            verifyNoInteractions(feedClient);
        }

        @Test
        @DisplayName("Updated takes place")
        void update()
        {
            var channel = new Channel("name", "url", 0);
            channel.setId(1L);

            when(repository.findById(channel.getId())).thenReturn(Optional.of(channel));

            service.update(channel.getId(), channel);

            verify(repository, times(1)).findById(channel.getId());
            verify(repository, times(1)).save(any());
            verifyNoMoreInteractions(repository);

            verifyNoInteractions(feedClient);
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

            when(repository.existsById(channelId)).thenReturn(false);

            assertThrows(ChannelNotFoundException.class, () -> service.delete(channelId));

            verify(repository, times(1)).existsById(channelId);
            verifyNoMoreInteractions(repository);

            verifyNoInteractions(feedClient);
        }

        @Test
        @DisplayName("Delete takes place")
        void delete()
        {
            var channelId = 1L;

            when(repository.existsById(channelId)).thenReturn(true);

            service.delete(channelId);

            verify(repository, times(1)).existsById(channelId);
            verify(repository, times(1)).deleteById(channelId);

            verifyNoMoreInteractions(repository);

            verifyNoInteractions(feedClient);
        }
    }

    @Nested
    @DisplayName("Test the updateRefreshTime() method")
    class UpdateRefreshTime
    {
        @Test
        void updateRefreshTime()
        {
            var channelId = 1L;

            service.updateRefreshTime(channelId);

            verify(repository, times(1)).updateRefreshTime(anyLong(), any(ZonedDateTime.class));
            verifyNoMoreInteractions(repository);

            verifyNoInteractions(feedClient);
        }
    }
}