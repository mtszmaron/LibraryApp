package services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.Person;

import java.util.List;

public interface GetPersonServiceInterface {
    public default List<Person> getAllPeople(final APIGatewayProxyRequestEvent input, final Context context){
        return null;
    }
    public default Person getPersonById(APIGatewayProxyRequestEvent input, Context context){
        return null;
    }
    public default List<Person> getPersonByName(APIGatewayProxyRequestEvent input, Context context){
        return null;
    }
}
