package com.assignment.aggregator.controllers;

import com.assignment.aggregator.dto.ChannelDTO;
import com.assignment.aggregator.exceptions.ChannelNotFoundException;
import com.assignment.aggregator.exceptions.DuplicatedChannelException;
import com.assignment.aggregator.mappers.IMapper;
import com.assignment.aggregator.models.Channel;
import com.assignment.aggregator.services.IChannelService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContext;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChannelControllerTest extends AbstractControllerTest
{

    @MockBean
    private IChannelService service;

    @MockBean
    private IMapper<Channel, ChannelDTO> mapper;

    @Test
    public void testContext()
    {
        ServletContext servletContext = context.getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);

        Assertions.assertNotNull(context.getBean("channelController"));
    }

    @Nested
    class TestGetChannelList
    {
        @Test
        void getChannelList_NoResponse() throws Exception
        {

            when(service.list()).thenReturn(List.of());

            var result = mockMvc.perform(get("/channel/"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$", hasSize(0)))
                                .andReturn();

            assertJsonResponse(result);

            verify(service, times(1)).list();
            verifyNoMoreInteractions(service);
        }

        @Test
        void getChannelList_ReturnsList() throws Exception
        {
            var channel = new Channel("name", "url", 0);
            channel.setId(1L);

            when(service.list()).thenReturn(List.of(channel));

            var result = mockMvc.perform(get("/channel/"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].id", is(channel.getId().intValue())))
                                .andExpect(jsonPath("$[0].name", is(channel.getName())))
                                .andExpect(jsonPath("$[0].url", is(channel.getUrl())))
                                .andExpect(jsonPath("$[0].ttl", is(channel.getTtl())))
                                .andReturn();

            assertJsonResponse(result);

            verify(service, times(1)).list();
            verifyNoMoreInteractions(service);
        }
    }

    @Nested
    class TestGetChannel
    {
        @Test
        void getChannel_UnknownChannel() throws Exception
        {
            var channelId = 1L;

            when(service.get(channelId)).thenThrow(new ChannelNotFoundException(channelId));

            var result = mockMvc.perform(get("/channel/{channelId}", channelId))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status", is(404)))
                                .andExpect(jsonPath("$.error", is("Not Found")))
                                .andExpect(jsonPath("$.message", is("Unknown channel [id: " + channelId + "]")))
                                .andExpect(jsonPath("$.httpMethod", is("GET")))
                                .andExpect(jsonPath("$.path", is("/channel/" + channelId)))
                                .andReturn();

            assertJsonResponse(result);

            verify(service, times(1)).get(channelId);
            verifyNoMoreInteractions(service);
        }

        @Test
        void getChannel() throws Exception
        {
            var channel = new Channel("name", "url", 0);
            channel.setId(1L);

            when(service.get(channel.getId())).thenReturn(channel);

            var result = mockMvc.perform(get("/channel/{channelId}", channel.getId()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(channel.getId().intValue())))
                                .andExpect(jsonPath("$.name", is(channel.getName())))
                                .andExpect(jsonPath("$.url", is(channel.getUrl())))
                                .andExpect(jsonPath("$.ttl", is(channel.getTtl())))
                                .andReturn();

            assertJsonResponse(result);

            verify(service, times(1)).get(channel.getId());
            verifyNoMoreInteractions(service);
        }

    }

    @Nested
    class TestCreateChannel
    {
        @Test
        void createChannel_InvalidDTO() throws Exception
        {
            var channel = new Channel("name", null, 0);
            channel.setId(1L);

            when(mapper.map(ArgumentMatchers.any(ChannelDTO.class), eq(Channel.class))).thenReturn(channel);
            when(service.create(ArgumentMatchers.any(Channel.class))).thenThrow(new ConstraintViolationException(Set.of()));

            var result = mockMvc.perform(post("/channel/")
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .content(objectMapper.writeValueAsString(channel))
                                                 .accept(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status", is(400)))
                                .andExpect(jsonPath("$.error", is("Bad Request")))
                                .andExpect(jsonPath("$.message", emptyString()))
                                .andExpect(jsonPath("$.httpMethod", is("POST")))
                                .andExpect(jsonPath("$.path", is("/channel/")))
                                .andReturn();

            assertJsonResponse(result);

            verify(mapper, times(1)).map(ArgumentMatchers.any(ChannelDTO.class), eq(Channel.class));
            verifyNoMoreInteractions(mapper);

            verify(service, times(1)).create(channel);
            verifyNoMoreInteractions(service);
        }

        @Test
        void createChannel_DuplicatedChannel() throws Exception
        {
            var channel = new Channel("name", "url", 0);
            channel.setId(1L);

            when(mapper.map(ArgumentMatchers.any(ChannelDTO.class), eq(Channel.class))).thenReturn(channel);
            when(service.create(ArgumentMatchers.any(Channel.class))).thenThrow(new DuplicatedChannelException(channel.getUrl()));

            var result = mockMvc.perform(post("/channel/")
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .content(objectMapper.writeValueAsString(channel))
                                                 .accept(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status", is(409)))
                                .andExpect(jsonPath("$.error", is("Conflict")))
                                .andExpect(jsonPath("$.message", is("You are already subscribed to this channel [url: " + channel.getUrl() + "]")))
                                .andExpect(jsonPath("$.httpMethod", is("POST")))
                                .andExpect(jsonPath("$.path", is("/channel/")))
                                .andReturn();

            assertJsonResponse(result);

            verify(mapper, times(1)).map(ArgumentMatchers.any(ChannelDTO.class), eq(Channel.class));
            verifyNoMoreInteractions(mapper);

            verify(service, times(1)).create(channel);
            verifyNoMoreInteractions(service);
        }

        @Test
        void createChannel() throws Exception
        {
            var channel = new Channel("name", "url", 0);
            channel.setId(1L);

            when(mapper.map(ArgumentMatchers.any(ChannelDTO.class), eq(Channel.class))).thenReturn(channel);
            when(service.create(any())).thenReturn(channel);

            var result = mockMvc.perform(post("/channel/")
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .content(objectMapper.writeValueAsString(channel))
                                                 .accept(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id", is(channel.getId().intValue())))
                                .andExpect(jsonPath("$.name", is(channel.getName())))
                                .andExpect(jsonPath("$.url", is(channel.getUrl())))
                                .andExpect(jsonPath("$.ttl", is(channel.getTtl())))
                                .andExpect(header().string("Location", "http://localhost/channel/" + channel.getId()))
                                .andReturn();

            assertJsonResponse(result);

            verify(mapper, times(1)).map(ArgumentMatchers.any(ChannelDTO.class), eq(Channel.class));
            verifyNoMoreInteractions(mapper);

            verify(service, times(1)).create(channel);
            verifyNoMoreInteractions(service);
        }

    }

    @Nested
    class TestUpdateChannel
    {
        @Test
        void updateChannel_InvalidDTO() throws Exception
        {
            var channel = new Channel("name", null, 0);
            channel.setId(1L);

            when(mapper.map(ArgumentMatchers.any(ChannelDTO.class), eq(Channel.class))).thenReturn(channel);
            when(service.update(eq(channel.getId()), ArgumentMatchers.any(Channel.class))).thenThrow(new ConstraintViolationException(Set.of()));

            var result = mockMvc.perform(put("/channel/{channelId}", channel.getId())
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .content(objectMapper.writeValueAsString(channel))
                                                 .accept(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status", is(400)))
                                .andExpect(jsonPath("$.error", is("Bad Request")))
                                .andExpect(jsonPath("$.message", emptyString()))
                                .andExpect(jsonPath("$.httpMethod", is("PUT")))
                                .andExpect(jsonPath("$.path", is("/channel/" + channel.getId())))
                                .andReturn();

            assertJsonResponse(result);

            verify(mapper, times(1)).map(ArgumentMatchers.any(ChannelDTO.class), eq(Channel.class));
            verifyNoMoreInteractions(mapper);

            verify(service, times(1)).update(channel.getId(), channel);
            verifyNoMoreInteractions(service);
        }

        @Test
        void updateChannel_ChannelNotFound() throws Exception
        {
            var channel = new Channel("name", "url", 0);
            channel.setId(1L);

            when(mapper.map(ArgumentMatchers.any(ChannelDTO.class), eq(Channel.class))).thenReturn(channel);
            when(service.update(anyLong(), ArgumentMatchers.any(Channel.class))).thenThrow(new ChannelNotFoundException(channel.getId()));

            var result = mockMvc.perform(put("/channel/{channelId}", channel.getId())
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .content(objectMapper.writeValueAsString(channel))
                                                 .accept(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status", is(404)))
                                .andExpect(jsonPath("$.error", is("Not Found")))
                                .andExpect(jsonPath("$.message", is("Unknown channel [id: " + channel.getId() + "]")))
                                .andExpect(jsonPath("$.httpMethod", is("PUT")))
                                .andExpect(jsonPath("$.path", is("/channel/" + channel.getId())))
                                .andReturn();

            assertJsonResponse(result);

            verify(mapper, times(1)).map(ArgumentMatchers.any(ChannelDTO.class), eq(Channel.class));
            verifyNoMoreInteractions(mapper);

            verify(service, times(1)).update(channel.getId(), channel);
            verifyNoMoreInteractions(service);
        }

        @Test
        void updateChannel() throws Exception
        {
            var channel = new Channel("name", "url", 0);
            channel.setId(1L);

            when(mapper.map(ArgumentMatchers.any(ChannelDTO.class), eq(Channel.class))).thenReturn(channel);
            when(service.update(anyLong(), any())).thenReturn(channel);

            var result = mockMvc.perform(put("/channel/{channelId}", channel.getId())
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .content(objectMapper.writeValueAsString(channel))
                                                 .accept(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(channel.getId().intValue())))
                                .andExpect(jsonPath("$.name", is(channel.getName())))
                                .andExpect(jsonPath("$.url", is(channel.getUrl())))
                                .andExpect(jsonPath("$.ttl", is(channel.getTtl())))
                                .andReturn();

            assertJsonResponse(result);

            verify(mapper, times(1)).map(ArgumentMatchers.any(ChannelDTO.class), eq(Channel.class));
            verifyNoMoreInteractions(mapper);

            verify(service, times(1)).update(channel.getId(), channel);
            verifyNoMoreInteractions(service);
        }

    }

    @Nested
    class TestDeleteChannel
    {
        @Test
        void deleteChannel_UnknownChannel() throws Exception
        {
            var channelId = 1L;

            doThrow(new ChannelNotFoundException(channelId)).when(service).delete(channelId);

            var result = mockMvc.perform(delete("/channel/{channelId}", channelId))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status", is(404)))
                                .andExpect(jsonPath("$.error", is("Not Found")))
                                .andExpect(jsonPath("$.message", is("Unknown channel [id: " + channelId + "]")))
                                .andExpect(jsonPath("$.httpMethod", is("DELETE")))
                                .andExpect(jsonPath("$.path", is("/channel/" + channelId)))
                                .andReturn();

            assertJsonResponse(result);

            verify(service, times(1)).delete(channelId);
            verifyNoMoreInteractions(service);
        }

        @Test
        void deleteChannel() throws Exception
        {
            var channelId = 1L;
            mockMvc.perform(delete("/channel/{channelId}", channelId))
                   .andDo(print())
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$").doesNotExist())
                   .andReturn();

            verify(service, times(1)).delete(channelId);
            verifyNoMoreInteractions(service);
        }

    }
}