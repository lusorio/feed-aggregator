package com.assignment.aggregator.controllers;

import com.assignment.aggregator.client.IFeedClient;
import com.assignment.aggregator.exceptions.exceptionhandler.CustomErrorResponse;
import com.assignment.aggregator.models.Channel;
import com.assignment.aggregator.models.FeedEntry;
import com.assignment.aggregator.services.IFeedService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/feed")
@Validated
@Api(tags = {"Feed"})
public class FeedController
{
    private final IFeedService feedService;

    /**
     * Implicit constructor injection is used
     *
     * @param feedService service providing aggregation operations
     */
    FeedController(final IFeedService feedService)
    {
        this.feedService = feedService;
    }

    /**
     * Return the list of entries published for a given {@link Channel}
     * <p>
     * When a channel is fetched, an implementation of a {@link IFeedClient} will request the channel's URL
     * to retrieve the current state of the feed. Once fetch, the feed's entries are collected and returned to the user.
     * <p>
     * Channels have a TTL property that indicate the system for how often the channel's feed is considered fresh. Within a
     * fetch operation, the channel's TTL will be examined to determine whether the content must be refreshed. If the last update
     * time for a channel doesn't exceed the channel's TTL, the channel's feed is considered fresh and won't be refreshed.
     * <p>
     * This feature can be overridden with the <code>forceRefresh</code> parameter, forcin all the channels to be
     * fetch even while in unexpired TTLs.
     *
     * @param id           the id of the channel to fetch
     * @param forceRefresh determines if the feed must be refreshed even if the {@link Channel}'s TTL has not yet expired
     * @return the {@link Channel}'s list of entries
     */
    @GetMapping("/channel/{id}")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "Fetch a channel",
                  notes = "Fetch the up to date web syndication feed from a subscribed channel",
                  response = FeedEntry.class,
                  responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Channel not found", response = CustomErrorResponse.class)
    })
    public Collection<FeedEntry> fetch(@PathVariable("id") long id, @RequestParam(name = "forceRefresh", required = false) boolean forceRefresh)
    {
        return feedService.fetch(id, forceRefresh);
    }

    /**
     * Aggregate entries from all subscribed {@link Channel}
     * <p>
     * This operation aggregates the entries of all the eligible channels in one single custom feed. When aggregating, the list
     * of subscribed channels will be curated to those having an expired TTL (unless overridden by <code>forceRefresh</code>)
     * Every eligible channel will then be fetched in parallel and its entries aggregated in a single list of entries which will be then
     * provided to the client.
     * <p>
     * This operation doesn't feature any treatment of the feed such as filtering or sorting.
     *
     * @param forceRefresh determines if {@link Channel} must be refreshed even if their TTLs have not yet expired
     * @return the list of combined entries
     */
    @GetMapping(value = "/aggregate")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "Aggregate channels",
                  notes = "Aggregate the contents of all the subscribed channels.",
                  response = FeedEntry.class,
                  responseContainer = "List")
    public Collection<FeedEntry> aggregate(@RequestParam(name = "forceRefresh", required = false) boolean forceRefresh)
    {
        return feedService.aggregate(forceRefresh);
    }
}
