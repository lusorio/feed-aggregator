package com.assignment.aggregator.client;

import com.assignment.aggregator.AbstractSpringTest;
import com.assignment.aggregator.exceptions.InvalidChannelException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

class FeedClientTest extends AbstractSpringTest
{

    @Autowired
    private FeedClient client;

    //    try(
    //    XmlReader reader = new XmlReader(new URL(urlString)))
    //
    //    {
    //        return new SyndFeedInput().build(reader);
    //    }
    //        catch(
    //    FeedException e)
    //
    //    {
    //        throw new InvalidChannelException(urlString);
    //    }
    //        catch(
    //    Exception e)
    //
    //    {
    //        throw new InvalidChannelException(e);
    //    }

    @Nested
    @DisplayName(value = "Test the fetch() method")
    class Fetch
    {
        @ParameterizedTest
        @ValueSource(strings = {" ", "malformedURL"})
        @NullAndEmptySource
        @DisplayName("Assert that an InvalidChannelException is thrown if a malformed URL the provided")
        void fetch_MalformedURL(String url)
        {
            Assertions.assertThrows(InvalidChannelException.class, () -> client.fetch(url));
        }

        @ParameterizedTest
        @ValueSource(strings = {"http://www.google.com", "https://domain.io/rss"})
        @DisplayName("Assert that an InvalidChannelException is thrown if the provided URL is not a valid feed source")
        void fetch_NotAValidSource(String url)
        {
            Assertions.assertThrows(InvalidChannelException.class, () -> client.fetch(url));
        }

        @ParameterizedTest
        @ValueSource(strings = {"https://vladmihalcea.com/feed/"})
        @DisplayName("Assert that no exception is thrown when a valid source URL is provided")
        void fetch(String url)
        {
            var result = client.fetch(url);

            Assertions.assertAll(
                    () -> Assertions.assertNotNull(result),
                    () -> Assertions.assertEquals("rss_2.0", result.getFeedType()));
        }
    }

}