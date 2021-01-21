package com.assignment.aggregator.controllers;

import com.assignment.aggregator.dto.FeedEntryDTO;
import com.assignment.aggregator.mappers.IMapper;
import com.assignment.aggregator.services.IFeedService;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedControllerTest extends AbstractControllerTest
{

    @MockBean
    private IFeedService service;

    @MockBean
    private IMapper<SyndEntry, FeedEntryDTO> mapper;

    @Test
    public void testContext()
    {
        ServletContext servletContext = context.getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);

        Assertions.assertNotNull(context.getBean("feedController"));
    }

    @Nested
    class TestFetch
    {

        @Test
        void fetch_NoResponse() throws Exception
        {

            var channelId = 1L;

            when(service.fetch(anyLong(), anyBoolean())).thenReturn(List.of());

            var result = mockMvc.perform(get("/feed/channel/{channelId}", channelId)
                                                 .param("forceRefresh", Boolean.TRUE.toString()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$", hasSize(0)))
                                .andReturn();

            assertJsonResponse(result);

            verify(service, times(1)).fetch(channelId, true);
            verifyNoMoreInteractions(service);

            verify(mapper, times(1)).mapToDTO(List.of(), FeedEntryDTO.class);
            verifyNoMoreInteractions(mapper);
        }

        @Test
        void fetch_ReturnsList() throws Exception
        {
            var channelId = 1L;

            var entries = new ArrayList<SyndEntry>();
            entries.add(new SyndEntryImpl());

            var dto = List.of(new FeedEntryDTO());

            when(service.fetch(anyLong(), anyBoolean())).thenReturn(entries);
            when(mapper.mapToDTO(anyList(), eq(FeedEntryDTO.class))).thenReturn(dto);

            var result = mockMvc.perform(get("/feed/channel/{channelId}", channelId)
                                                 .param("forceRefresh", Boolean.TRUE.toString()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andReturn();

            assertJsonResponse(result);

            verify(service, times(1)).fetch(channelId, true);
            verifyNoMoreInteractions(service);

            verify(mapper, times(1)).mapToDTO(entries, FeedEntryDTO.class);
            verifyNoMoreInteractions(mapper);
        }
    }

    @Nested
    class TestAggregate
    {
        @Test
        void aggregate_NoResponse() throws Exception
        {
            when(service.aggregate(anyBoolean())).thenReturn(List.of());

            var result = mockMvc.perform(get("/feed/aggregate/")
                                                 .param("forceRefresh", Boolean.TRUE.toString()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$", hasSize(0)))
                                .andReturn();

            assertJsonResponse(result);

            verify(service, times(1)).aggregate(true);
            verifyNoMoreInteractions(service);

            verify(mapper, times(1)).mapToDTO(List.of(), FeedEntryDTO.class);
            verifyNoMoreInteractions(service);
        }

        @Test
        void aggregate_ReturnsList() throws Exception
        {
            var entries = new ArrayList<SyndEntry>();
            entries.add(new SyndEntryImpl());

            var dto = List.of(new FeedEntryDTO());

            when(service.aggregate(anyBoolean())).thenReturn(entries);
            when(mapper.mapToDTO(entries, FeedEntryDTO.class)).thenReturn(dto);

            var result = mockMvc.perform(get("/feed/aggregate/")
                                                 .param("forceRefresh", Boolean.TRUE.toString()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andReturn();

            assertJsonResponse(result);

            verify(service, times(1)).aggregate(true);
            verifyNoMoreInteractions(service);

            verify(mapper, times(1)).mapToDTO(entries, FeedEntryDTO.class);
            verifyNoMoreInteractions(mapper);
        }
    }
}