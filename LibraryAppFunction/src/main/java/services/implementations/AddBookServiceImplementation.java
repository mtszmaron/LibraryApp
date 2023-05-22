package services.implementations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.AddBookServiceInterface;
import util.GeneralException;

import java.util.Map;

//Implementation of interface in charge of adding books to database
public class AddBookServiceImplementation implements AddBookServiceInterface {

    private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private DynamoDBMapper mapper = new DynamoDBMapper(client);

    private static final Logger logger = LoggerFactory.getLogger(AddBookServiceImplementation.class);

    //Method: add single book with specified title and category
    public Book addBook(APIGatewayProxyRequestEvent input, Context context){
        logger.info(input.toString());
        try{
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            final String title = pathParameters.get("title");
            final String category = pathParameters.get("category");

            if(title == null || title.trim().isEmpty()) throw new GeneralException("Title can not be empty");
            if(category == null || category.trim().isEmpty()) throw new GeneralException("Category can not be empty");

            Book book = new Book();
            book.setTitle(title);
            book.setCategory(category);
            book.setStatus("available");
            logger.info("Mapping");
            logger.info(title);
            mapper.save(book);
            logger.info("Mapping done");
            return book;
        }
        catch(Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during adding new book");
        }
    }
}
