package com.assignment.aggregator.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.text.MessageFormat;

/**
 * This helper provides a way to build the API path being called by the current
 * {@link org.springframework.web.context.request.WebRequest}
 */
public class ResourceLocationHelper
{

    private static final Logger logger = LoggerFactory.getLogger(ResourceLocationHelper.class);

    private ResourceLocationHelper()
    {
        // hide public constructor
    }

    /**
     * Builds the API path for the current {@link org.springframework.web.context.request.WebRequest}
     * <p>
     * For every request, this helper can return the complete path of the endpoint being called. It is useful
     * to determine the location of newly created resources, which should be included in the Location header
     * of the response in POST request, where the response is 201 Created.
     *
     * @param variable
     * @param value
     * @return the complete API path for the current {@link org.springframework.web.context.request.WebRequest}
     */
    public static URI getResourceLocation(String variable, Object value)
    {
        var location = ServletUriComponentsBuilder
                               .fromCurrentRequest()
                               .pathSegment(variable)
                               .buildAndExpand(value)
                               .toUri();

        if (logger.isInfoEnabled())
        {
            logger.info(MessageFormat.format("Current location : {0}", location));
        }

        return location;
    }
}
