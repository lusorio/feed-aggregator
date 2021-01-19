package com.assignment.aggregator.client;

import com.assignment.aggregator.exceptions.InvalidChannelException;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class FeedClient implements IFeedClient
{
    public SyndFeed fetch(String urlString)
    {
        try (XmlReader reader = new XmlReader(new URL(urlString)))
        {
            return new SyndFeedInput().build(reader);
        }
        catch (FeedException e)
        {
            throw new InvalidChannelException(urlString);
        }
        catch (Exception e)
        {
            throw new InvalidChannelException(e);
        }
    }
}
