package com.serverless.services;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import org.apache.log4j.Logger;

/**
 * Service class to give access to parameters stored on AWS Parameter Store
 */
public class SecureParameterService {
    private static final Logger LOG = Logger.getLogger(SecureParameterService.class);

    private static String superSecretApiKey;

    private SecureParameterService(){}

    public static String getParameterValue(String name, boolean withDecryption){
        final AWSSimpleSystemsManagement client = AWSSimpleSystemsManagementClientBuilder.defaultClient();

        GetParameterRequest request = new GetParameterRequest();
        request.withName(name).setWithDecryption(withDecryption);

        GetParameterResult result = client.getParameter(request);

        LOG.debug("SSM result for param "+name+": "+result);

        if (result.getParameter() != null){
            return result.getParameter().getValue();
        } else {
            return null;
        }
    }

    public static String getStageName(){
        String stage = System.getenv("STAGE");
        if (stage == null) stage = "development";

        return stage;
    }

    public static String getSuperSecretApiKey() {
        if (superSecretApiKey != null) return superSecretApiKey;

        String paramName = "/"+getStageName()+"/mysecrets/apikey";
        superSecretApiKey = getParameterValue(paramName, true);

        if (superSecretApiKey == null) throw new RuntimeException("Super secret is NULL!");

        return superSecretApiKey;
    }


}
