package com.assignment.aggregator.controllers;

import com.assignment.aggregator.dto.FeedEntryDTO;
import com.assignment.aggregator.mappers.IMapper;
import com.assignment.aggregator.models.Channel;
import com.assignment.aggregator.services.IFeedService;
import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feed")
@Validated
public class FeedController
{
    private final IFeedService feedService;

    private final IMapper<SyndEntry, FeedEntryDTO> mapper;

    /**
     * Implicit constructor injection is used
     *
     * @param feedService service providing aggregation operations
     * @param mapper      service providing mapping for {@link SyndEntry} entities
     */
    FeedController(final IFeedService feedService, final IMapper<SyndEntry, FeedEntryDTO> mapper)
    {
        this.feedService = feedService;
        this.mapper = mapper;
    }

    /**
     * Return the list of entries published for a given {@link Channel}
     *
     * @param id           the id of the channel
     * @param forceRefresh determines if the feed must be refreshed even if the {@link Channel}'s TTL has not yet expired
     * @return the {@link Channel}'s list of entries
     */
    @GetMapping("/channel/{id}")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public List<FeedEntryDTO> fetch(@PathVariable("id") long id, @RequestParam(name = "forceRefresh", required = false) boolean forceRefresh)
    {
        return mapper.mapToDTO(feedService.fetch(id, forceRefresh), FeedEntryDTO.class);
    }

    /**
     * Aggregate entries from all subscribed {@link Channel}
     *
     * @param forceRefresh determines if {@link Channel} must be refreshed even if their TTLs have not yet expired
     * @return the list of combined entries
     */
    @GetMapping(value = "/aggregate")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public List<FeedEntryDTO> aggregate(@RequestParam(name = "forceRefresh", required = false) boolean forceRefresh)
    {
        return mapper.mapToDTO(feedService.aggregate(forceRefresh), FeedEntryDTO.class);
    }
}
