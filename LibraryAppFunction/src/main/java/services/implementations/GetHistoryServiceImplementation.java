package services.implementations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.GetHistoryServiceInterface;
import util.GeneralException;

import java.util.List;
import java.util.Map;

//Implementation of interface in charge of returning history type records based on input
public class GetHistoryServiceImplementation implements GetHistoryServiceInterface {

    private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private DynamoDBMapper mapper = new DynamoDBMapper(client);

    private static final Logger logger = LoggerFactory.getLogger(GetHistoryServiceImplementation.class);

    //Method: returns all history records from database based on given book id (returns book's renting history)
    public List<History> getBookHistory(final APIGatewayProxyRequestEvent input, final Context context){
        try{
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            final String id = pathParameters.get("bookId");

            logger.info("Getting book's history of id: " + id);
            History history = new History();
            history.setBookId(id);
            DynamoDBQueryExpression<History> queryExpression = new DynamoDBQueryExpression<History>()
                    .withIndexName("bookIdIndex")
                    .withHashKeyValues(history)
                    .withConsistentRead(false);
            List<History> bookHistory = mapper.query(History.class, queryExpression);

            return bookHistory;
        }
        catch (Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during getting book history");
        }
    }

    //Method: returns all history records from database based on given person id (returns given person full data and it's renting history)
    public List<History> getPersonHistory(APIGatewayProxyRequestEvent input, Context context){
        try{
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            final String id = pathParameters.get("personId");
            logger.info("Getting person data and history by id: " + id);
            History history = new History();
            history.setRentedBy(id);

            DynamoDBQueryExpression<History> queryExpression = new DynamoDBQueryExpression<History>()
                    .withIndexName("rentedByIndex")
                    .withHashKeyValues(history)
                    .withConsistentRead(false);

            List<History> historyList = mapper.query(History.class, queryExpression);
            logger.info("Found records: " + historyList.size());
            return historyList;
        }
        catch (Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during getting person history");
        }
    }
}
