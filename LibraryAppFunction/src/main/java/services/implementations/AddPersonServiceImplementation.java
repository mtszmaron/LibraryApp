package services.implementations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.AddPersonServiceInterface;
import util.GeneralException;

import java.util.Map;

//Implementation of interface in charge of adding person records to database
public class AddPersonServiceImplementation implements AddPersonServiceInterface {

    private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private DynamoDBMapper mapper = new DynamoDBMapper(client);

    private static final Logger logger = LoggerFactory.getLogger(AddPersonServiceImplementation.class);

    //Method: adds person to database with given data from request: name, second name, last name, email
    public Person addPerson(APIGatewayProxyRequestEvent input, Context context){
        try {
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            final String name = pathParameters.get("name");
            final String secondName = pathParameters.get("secondName");
            final String lastName = pathParameters.get("lastName");
            final String email = pathParameters.get("email");

            if(name == null || name.trim().isEmpty()) throw new GeneralException("Name can not be empty");
            if(secondName == null || secondName.trim().isEmpty()) throw new GeneralException("Second name can not be empty");
            if(lastName == null || lastName.trim().isEmpty()) throw new GeneralException("Last name can not be empty");
            if(email == null || email.trim().isEmpty()) throw new GeneralException("Email can not be empty");

            Person person = new Person();
            person.setName(name);
            person.setSecondName(secondName);
            person.setLastName(lastName);
            person.setEmail(email);
            mapper.save(person);
            return person;
        }
        catch(Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during adding person");
        }
    }
}
