package com.wildsoft.core.lambda;

import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class TestHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final MyInvokableHandler.MyHandlerService myHandlerService;

    public TestHandler(){
        myHandlerService = LambdaInvokerFactory.builder()
                .lambdaClient(AWSLambdaClientBuilder.defaultClient())
                .build(MyInvokableHandler.MyHandlerService.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Some testing request received");

        invokeMyHandler(new MyInvokableHandler.MyCustomRequest("Hello", "non"));

        return new APIGatewayProxyResponseEvent().withStatusCode(200)
                .withBody("It works!");
    }

    private MyInvokableHandler.SomeResponse invokeMyHandler(MyInvokableHandler.MyCustomRequest request){
        return myHandlerService.sendCustomRequest(request);
    }

}
