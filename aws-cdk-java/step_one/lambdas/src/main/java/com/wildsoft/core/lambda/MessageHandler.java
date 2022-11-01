package com.wildsoft.core.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageHandler implements RequestHandler<MessageHandler.Message, String> {
    private static final Logger LOG = LogManager.getLogger(MessageHandler.class);

    @Override
    public String handleRequest(Message request, Context context) {
        LOG.info("Some request received: "+request.getMessage());

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
    }
}
