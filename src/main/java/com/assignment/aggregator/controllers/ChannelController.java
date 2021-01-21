package com.assignment.aggregator.controllers;

import com.assignment.aggregator.dto.ChannelDTO;
import com.assignment.aggregator.helpers.ResourceLocationHelper;
import com.assignment.aggregator.mappers.IMapper;
import com.assignment.aggregator.models.Channel;
import com.assignment.aggregator.services.IChannelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/channel")
@Validated
public class ChannelController
{
    private final IChannelService channelService;

    private final IMapper<Channel, ChannelDTO> mapper;

    /**
     * Implicit constructor injection is used
     *
     * @param channelService service providing channel operations
     * @param mapper         service providing mapping for {@link Channel} entities
     */
    ChannelController(final IChannelService channelService, final IMapper<Channel, ChannelDTO> mapper)
    {
        this.channelService = channelService;
        this.mapper = mapper;
    }

    /**
     * Fetch the list of subscribed channels
     *
     * @return the list of subscribed channels
     */
    @GetMapping("/")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public List<Channel> getChannelList()
    {
        return channelService.list();
    }

    /**
     * Fetch a subscribed channel based on its id
     *
     * @return the subscribed channel that matches the given id
     */
    @GetMapping("/{id}")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public Channel getChannel(@Positive @PathVariable Long id)
    {
        return channelService.get(id);
    }

    /**
     * Create a new subscription to a syndication channel
     *
     * @param channelDTO a representation of the object needed to create a subscription
     * @return the subscribed channel
     */
    @PostMapping("/")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<Channel> createChannel(@RequestBody ChannelDTO channelDTO)
    {
        var newChannel = channelService.create(mapper.map(channelDTO, Channel.class));

        return ResponseEntity.created(ResourceLocationHelper.getResourceLocation("{id}", newChannel.getId()))
                             .body(newChannel);
    }

    /**
     * Update a subscribed channel
     *
     * @param id         the id of the channel's subscription being updated
     * @param channelDTO a representation of the updated channel subscription
     * @return the updated channel
     */
    @PutMapping("/{id}")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public Channel updateChannel(@Positive @PathVariable Long id, @RequestBody ChannelDTO channelDTO)
    {
        return channelService.update(id, mapper.map(channelDTO, Channel.class));
    }

    /**
     * Delete a subscription to a channel
     *
     * @param id the id of the subscribed channel
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteChannel(@Positive @PathVariable Long id)
    {
        channelService.delete(id);
    }
}
