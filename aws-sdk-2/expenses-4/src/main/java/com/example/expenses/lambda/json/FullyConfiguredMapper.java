package com.example.expenses.lambda.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * TODO: javadoc FullyConfiguredMapper
 */
public class FullyConfiguredMapper extends ObjectMapper
{
    public FullyConfiguredMapper(){
        super();
        registerModule( new JavaTimeModule() );
        disable( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS );
    }
}