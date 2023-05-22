package services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public interface DeletePersonServiceInterface {
    public default void deletePerson(APIGatewayProxyRequestEvent input, Context context) { }
}
