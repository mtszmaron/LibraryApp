package services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.Person;

public interface AddPersonServiceInterface {
    public default Person addPerson(final APIGatewayProxyRequestEvent input, final Context context){
        return null;
    }
}
