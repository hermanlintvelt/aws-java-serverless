package com.wildsoft.core.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class MessageHandler implements RequestHandler<MessageHandler.Message, String> {
    @Override
    public String handleRequest(Message request, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Some request received: "+request);

        return "Greetings: "+request.getMessage();
    }

    public static class Message {
        private String message;

        public Message() {
        }

        public Message(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "message='" + message + '\'' +
                    '}';
        }
    }
}
