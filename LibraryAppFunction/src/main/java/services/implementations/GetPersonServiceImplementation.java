package services.implementations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.Book;
import dao.History;
import dao.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.GetPersonServiceInterface;
import util.GeneralException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Implementation of interface in charge of returning people type records based on input
public class GetPersonServiceImplementation implements GetPersonServiceInterface{

    private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private DynamoDBMapper mapper = new DynamoDBMapper(client);

    private static final Logger logger = LoggerFactory.getLogger(GetPersonServiceImplementation.class);

    //Method: returns all person objects stored in database
    public List<Person> getAllPeople(APIGatewayProxyRequestEvent input, Context context){
        try {
            HashMap<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
            eav.put(":val1", new AttributeValue().withS("person"));

            final HashMap<String, String> ean = new HashMap<>();
            ean.put("#type", "type");

            logger.info("Getting person list");
//        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
//                .withFilterExpression("#type = :val1")
//                .withExpressionAttributeValues(eav)
//                .withExpressionAttributeNames(ean);
//        List<Person> personList = mapper.scan(Person.class,scanExpression);

            Person person = new Person();
            logger.info(person.getId() + " " + person.getItemType() + " " + person.getName() + " " + person.getSecondName() + " " + person.getLastName() + " " + person.getEmail());

            DynamoDBQueryExpression<Person> queryExpression = new DynamoDBQueryExpression<Person>()
                    .withIndexName("typeIndex")
                    .withHashKeyValues(person)
                    .withConsistentRead(false);

            List<Person> personList = mapper.query(Person.class, queryExpression);
            logger.info("Returning person list");
            for (Person item : personList) {
                logger.info(item.getId() + " " + item.getName() + " " + item.getSecondName() + " " + item.getLastName() + " " + item.getEmail());
            }
            return personList;
        }
        catch (Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during getting all people");
        }
    }

    //Method: returns person object based on given id
    public Person getPersonById(APIGatewayProxyRequestEvent input, Context context){
        logger.info(input.toString());
        try{
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            final String id = pathParameters.get("personId");
            logger.info("Getting person by id: " + id);
            Person person = new Person();
            person.setId(id);
            person = mapper.load(person);
            return person;
        }
        catch (Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during getting person by id");
        }
    }

    //Method: returns list of person type objects based on given name
    public List<Person> getPersonByName(APIGatewayProxyRequestEvent input, Context context){
        try {
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            final String name = pathParameters.get("name");
            logger.info("Getting people id by name: " + name);
            Person person = new Person();
            person.setName(name);

            DynamoDBQueryExpression<Person> queryExpression = new DynamoDBQueryExpression<Person>()
                    .withIndexName("nameIndex")
                    .withHashKeyValues(person)
                    .withConsistentRead(false);

            List<Person> peopleList = mapper.query(Person.class, queryExpression);
            logger.info("Found records: " + peopleList.size());
            return peopleList;
        }
        catch (Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during getting people by name");
        }
    }

//    public List<Person> getPersonAndHistory(APIGatewayProxyRequestEvent input, Context context){
//        try{
//            final Map<String, String> pathParameters = input.getQueryStringParameters();
//            final String id = pathParameters.get("id");
//            logger.info("Getting person data and history by id: " + id);
//            Person person = new Person();
//            person.setId(id);
//            person = mapper.load(person);
//
//        }
//        catch(Exception e){
//            throw e;
//        }
//    }
//
//    public List<History> getPersonRentHistory(APIGatewayProxyRequestEvent input, Context context){
//        try{
//            final Map<String, String> pathParameters = input.getQueryStringParameters();
//            final String id = pathParameters.get("id");
//            logger.info("Getting person data and history by id: " + id);
//            History history = new History();
//            history.setRentedBy(id);
//
//            DynamoDBQueryExpression<History> queryExpression = new DynamoDBQueryExpression<History>()
//                    .withIndexName("rentedByIndex")
//                    .withHashKeyValues(history)
//                    .withConsistentRead(false);
//
//            List<History> historyList = mapper.query(History.class, queryExpression);
//
//        }
//        catch(Exception e){
//            throw e;
//        }
//    }

}
