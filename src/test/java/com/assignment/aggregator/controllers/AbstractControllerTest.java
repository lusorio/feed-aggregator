package com.assignment.aggregator.controllers;

import com.assignment.aggregator.AbstractSpringTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

abstract class AbstractControllerTest extends AbstractSpringTest
{
    protected MockMvc mockMvc;

    protected ObjectMapper objectMapper;

    @Autowired
    protected WebApplicationContext context;

    @BeforeEach
    void setup()
    {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
        objectMapper = new ObjectMapper();
    }

    void assertJsonResponse(MvcResult result)
    {
        Assertions.assertEquals("application/json", result.getResponse().getContentType());
    }

}