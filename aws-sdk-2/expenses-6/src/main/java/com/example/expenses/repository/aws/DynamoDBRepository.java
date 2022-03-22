package com.example.expenses.repository.aws;

import com.example.expenses.lambda.CreateExpenseHandlerRequestObject;
import com.example.expenses.model.Expense;
import com.example.expenses.model.Person;
import com.example.expenses.repository.DataRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Stores the expenses in a DynamoDB table. The name of the table is determined by the `STAGE` environment variable,
 * e.g. `expenses-development`, `expenses-production`
 *
 * TODO: to illustrate this for now we just store the Person's email address directly in an expense item; we need to rather bring in a separate Person table as well.
 */
public class DynamoDBRepository implements DataRepository {
    private static final Logger LOG = LogManager.getLogger(DynamoDBRepository.class);
    private final static DynamoDbClient DYNAMO_DB_CLIENT = DynamoDbClient.builder()
            .region(Region.AF_SOUTH_1).build();
    private final static DynamoDbEnhancedClient DB_ENHANCED_CLIENT = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(DYNAMO_DB_CLIENT).build();

    private final DynamoDbTable<ExpenseRecord> expensesTable;

    public DynamoDBRepository(){
        expensesTable = DB_ENHANCED_CLIENT.table("expenses-"+System.getenv("STAGE"), TableSchema.fromBean(ExpenseRecord.class));
    }

    @Override
    public Person addPerson(Person person) {
        //TODO: to illustrate this for now we just store the Person's email address directly in an expense item; we need to rather bring in a separate Person table as well.
        return person;
    }

    @Override
    public Optional<Person> findPerson(String email) {
        //TODO: to illustrate this for now we just store the Person's email address directly in an expense item; we need to rather bring in a separate Person table as well.
        return Optional.empty();
    }

    @Override
    public List<Person> allPersons() {
        //TODO: to illustrate this for now we just store the Person's email address directly in an expense item; we need to rather bring in a separate Person table as well.
        return Collections.emptyList();
    }

    @Override
    public Expense addExpense(Expense expense) {
        LOG.info("Persisting to DynamoDB: "+expense);
        expensesTable.putItem(ExpenseRecord.fromExpense(expense));
        return expense;
    }

    @Override
    public List<Expense> findExpensesPaidBy(Person person) {
        LOG.info("Finding in DynamoDB: expenses for "+person);

        //Build an Expression for a filtered query
        Map<String, AttributeValue> expressionValues = Map.of(":value", AttributeValue.builder()
                .s(person.getEmail())
                .build());

        Expression expression = Expression.builder()
                .expression("email = :value")
                .expressionValues(expressionValues)
                .build();

        // Get items in the Customer table and write out the ID value.
        List<Expense> expenses = expensesTable.query(r -> r.filterExpression(expression)).items().stream().map(ExpenseRecord::asExpense).collect(Collectors.toUnmodifiableList());
        LOG.info(" ** found "+expenses.size()+" expenses");
        return expenses;
    }

    @Override
    public List<Expense> allExpenses() {
        return null;
    }
}
