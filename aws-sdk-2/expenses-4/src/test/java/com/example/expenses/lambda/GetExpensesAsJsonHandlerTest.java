package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.example.expenses.model.Expense;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GetExpensesAsJsonHandlerTest {
    private GetExpensesAsJsonHandler testHandler;
    private Context testContext;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        testHandler = new GetExpensesAsJsonHandler();
        testContext = new NullContext();
    }

    @Test
    @DisplayName("Test that request to function returns list of expenses")
    void testGetExpenses(){
        List<Expense> result = testHandler.handleRequest(null, testContext);
        assertThat(result).asList().isNotEmpty();
        assertThat(result).asList().size().isEqualTo(3);
    }
}
