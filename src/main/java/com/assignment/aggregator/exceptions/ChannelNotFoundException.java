package com.assignment.aggregator.exceptions;

public class ChannelNotFoundException extends RuntimeException
{
    public ChannelNotFoundException(Long id)
    {
        super("Unknown channel [id: " + id + "]");
    }
}
