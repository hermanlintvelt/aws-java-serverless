package com.example.expenses.repository;

import com.example.expenses.repository.aws.DynamoDBRepository;
import com.example.expenses.repository.memory.InMemoryRepository;

/**
 * Looks at value of `STAGE` env var to determine which implementation of <code>DataRepository</code> to use.
 */
public class DataRepositoryFactory {
    private final static DataRepository DATA_REPOSITORY;

    static {
        if (System.getenv("STAGE") == null || System.getenv("STAGE").equalsIgnoreCase("testing")){
            DATA_REPOSITORY = new InMemoryRepository();
        } else {
            DATA_REPOSITORY = new DynamoDBRepository();
        }
    }

    public static DataRepository getDataRepository() {
        return DATA_REPOSITORY;
    }
}
