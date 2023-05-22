package services.implementations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.DeletePersonServiceInterface;
import util.GeneralException;

import java.util.Map;

//Implementation of interface in charge of deleting person records from database through setting their status to "removed"
public class DeletePersonServiceImplementation implements DeletePersonServiceInterface {

    private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private DynamoDBMapper mapper = new DynamoDBMapper(client);
    private static final Logger logger = LoggerFactory.getLogger(DeletePersonServiceImplementation.class);
    DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig.Builder()
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .build();

    //Method: removes single person record from database by given id
    public void deletePerson(APIGatewayProxyRequestEvent input, Context context){
        try {
            Map<String, String> pathParameters = input.getQueryStringParameters();
            String id = pathParameters.get("id");
            logger.info("removing person of id:" + id);
            Person person = new Person();
            person.setId(id);
            person.setStatus("removed");
            mapper.save(person, mapperConfig);
            logger.info("person removed");
        }
        catch (Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during deleting person");
        }

    }
}
