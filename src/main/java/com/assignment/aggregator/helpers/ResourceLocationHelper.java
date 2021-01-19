package com.assignment.aggregator.helpers;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * This helper provides a way to build the API path being called by the current
 * {@link org.springframework.web.context.request.WebRequest}
 */
public class ResourceLocationHelper
{

    private ResourceLocationHelper()
    {
        // hide public constructor
    }

    /**
     * Builds the complete API path for the current {@link org.springframework.web.context.request.WebRequest}
     *
     * @param uriVariables a map containing the path variable names along their values
     * @return the complete API path for the current {@link org.springframework.web.context.request.WebRequest}
     */
    public static URI getResourceLocation(Map<String, Object> uriVariables)
    {
        return ServletUriComponentsBuilder
                       .fromCurrentRequest()
                       .pathSegment(uriVariables.keySet().toArray(String[]::new))
                       .buildAndExpand(uriVariables)
                       .toUri();
    }
}
