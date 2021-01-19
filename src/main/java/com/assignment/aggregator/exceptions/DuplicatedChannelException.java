package com.assignment.aggregator.exceptions;

public class DuplicatedChannelException extends RuntimeException
{
    public DuplicatedChannelException(String url)
    {
        super("You are already subscribed to this channel [url: " + url + "]");
    }
}
