package com.assignment.aggregator.mappers;

import com.assignment.aggregator.AbstractSpringTest;
import com.assignment.aggregator.dto.ChannelDTO;
import com.assignment.aggregator.models.Channel;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.*;

class ChannelMapperTest extends AbstractSpringTest
{

    @InjectMocks
    private ChannelMapper mapper;

    @Test
    void testMap_NullTTLConvertsToDefault()
    {
        var channelDTO = new ChannelDTO();
        channelDTO.setName("name");
        channelDTO.setUrl("url");
        channelDTO.setTtl(null);

        var result = mapper.map(channelDTO, Channel.class);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(channelDTO.getName(), result.getName()),
                () -> assertEquals(channelDTO.getUrl(), result.getUrl()),
                () -> assertEquals(0, result.getTtl()),
                () -> assertNull(result.getCreated()),
                () -> assertNull(result.getCreated()),
                () -> assertNull(result.getLastRefresh()));
    }

    @Test
    void testMap_NotNullTTLGetsMapped()
    {
        var channelDTO = new ChannelDTO();
        channelDTO.setTtl(3600);

        var result = mapper.map(channelDTO, Channel.class);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(channelDTO.getTtl(), result.getTtl()));
    }
}