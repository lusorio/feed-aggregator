package com.assignment.aggregator.client;

import com.rometools.rome.feed.synd.SyndFeed;

/**
 * Web syndication client
 * <p>
 * Implementation of a feed client using the ROME library (https://rometools.github.io/rome/). Fetch a valid
 * RSS/ATOM source from a given URL
 *
 * @see "https://rometools.github.io/rome/"
 */
public interface IFeedClient
{
    /**
     * Fetch a valid RSS/ATOM source from a given URL
     * <p>
     * Creates a {@link SyndFeed} object containing the feed information and the list of published entries. Usage of ROME
     * comes with some extra advantages:
     *
     * <li>The ROME fetcher uses {@link java.net.URL} object for representing the source so URL syntax validation can be
     * delegated to this step.</li>
     *
     * <li>The ROME fetcher uses and {@link org.xml.sax.XMLReader} to parse the source content, so validation of a known
     * web syndication sources can be delegated to this step.</li>
     *
     * @param urlString the feed source URL to fetch
     * @return a {@link SyndFeed} object representing a web syndication feed
     * @throws com.assignment.aggregator.exceptions.InvalidChannelException if an invalid syndication feed source is provided
     *                                                                      or if anything goes wrong while parsing its content.
     */
    SyndFeed fetch(String urlString);
}
