package com.assignment.aggregator.exceptions;

public class InvalidChannelException extends RuntimeException
{
    public InvalidChannelException(Throwable cause)
    {
        super(cause);
    }

    public InvalidChannelException(String url)
    {
        super("The channel's URL isn't a valid feed source [url: " + url + "]");
    }
}
