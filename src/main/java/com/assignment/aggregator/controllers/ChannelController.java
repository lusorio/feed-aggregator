package com.assignment.aggregator.controllers;

import com.assignment.aggregator.dto.ChannelDTO;
import com.assignment.aggregator.exceptions.exceptionhandler.CustomErrorResponse;
import com.assignment.aggregator.helpers.ResourceLocationHelper;
import com.assignment.aggregator.mappers.IMapper;
import com.assignment.aggregator.models.Channel;
import com.assignment.aggregator.services.IChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.List;

/**
 * ChannelController
 * <p>
 * The {@link ChannelController} controller provides the channel related entry points to the API.
 * It provides a mean of performing CRUD operations over {@link Channel}s, which are a representation of
 * a web syndication feed subscription.
 */
@RestController
@RequestMapping("/channel")
@Validated
@Api(tags = {"Channel"})
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
     * <p>
     * This method is used to fetch the list of channel a user has been subscribed to.
     *
     * @return the list of subscribed channels or an empty list of no channel has not been yet subscribed
     */
    @GetMapping("/")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "Get the list of channel subscriptions",
                  notes = "Returns a list of all the feed channels the user is subscribed to. If no subscriptions exist the result will be an empty list.",
                  response = Channel.class,
                  responseContainer = "List")
    public List<Channel> getChannelList()
    {
        return channelService.list();
    }

    /**
     * Fetch a subscribed channel based on its id
     *
     * @param id the id of the channel to retrieve
     * @return the subscribed channel that matches the given id or a {@link CustomErrorResponse} object if no
     * channel matches the given id.
     */
    @GetMapping("/{id}")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "Get a subscribed channel",
                  notes = "Get a channel based on its primary id",
                  response = Channel.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Channel not found", response = CustomErrorResponse.class)
    })
    public Channel getChannel(@Positive @PathVariable Long id)
    {
        return channelService.get(id);
    }

    /**
     * Create a new subscription to a channel
     * <p>
     * Subscription to web syndication feeds are represented by channels. Users can subscribe to new channels
     * by posting a new valid feed source URL to this endpoint.
     *
     * @param channelDTO a representation of the object needed to create a subscription
     * @return the subscribed channel or a {@link CustomErrorResponse} if there is an error while
     * creating the subscription
     */
    @PostMapping("/")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiOperation(value = "Add a new channel subscription",
                  notes = "Create a subscription to a valid RSS/ATOM web syndication feed",
                  response = Channel.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request", response = CustomErrorResponse.class)
    })
    public ResponseEntity<Channel> createChannel(@RequestBody ChannelDTO channelDTO)
    {
        var newChannel = channelService.create(mapper.map(channelDTO, Channel.class));

        return ResponseEntity.created(ResourceLocationHelper.getResourceLocation("{id}", newChannel.getId()))
                             .body(newChannel);
    }

    /**
     * Update a subscribed channel
     * <p>
     * Provides a mean to update a channel's information, such as the channel's name or TTL. Note that the channel's
     * URL can't be modified.
     *
     * @param id         the id of the channel's subscription being updated
     * @param channelDTO a representation of the updated channel subscription
     * @return the updated channel or a {@link CustomErrorResponse} if there is an error in the update operation
     */
    @PutMapping("/{id}")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "Update an existing channel subscription",
                  notes = "Update a subscription's details such as subscription name or TTL",
                  response = Channel.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request", response = CustomErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found", response = CustomErrorResponse.class)
    })
    public Channel updateChannel(@Positive @PathVariable Long id, @RequestBody ChannelDTO channelDTO)
    {
        return channelService.update(id, mapper.map(channelDTO, Channel.class));
    }

    /**
     * Delete a subscription to a channel
     * <p>
     * Users can unsubscribe from channels by deleting a channel entry with this endpoint. Channels removed
     * from the list won't appear in the users aggregated feed anymore.
     *
     * @param id the id of the subscribed channel
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "Delete a channel subscription",
                  notes = "Removes the subscription to a feed channel based on the channel's id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request", response = CustomErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found", response = CustomErrorResponse.class)
    })
    public void deleteChannel(@Positive @PathVariable Long id)
    {
        channelService.delete(id);
    }
}
