package services.implementations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.DeleteBookServiceInterface;
import util.GeneralException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

//Implementation of interface in charge of deleting books from database through setting its status to "removed"
public class DeleteBookServiceImplementation implements DeleteBookServiceInterface {

    private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private DynamoDBMapper mapper = new DynamoDBMapper(client);
    private static final Logger logger = LoggerFactory.getLogger(DeleteBookServiceImplementation.class);

    DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig.Builder()
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .build();

    //Method: removes singular book record from database
    @Override
    public String deleteBook(APIGatewayProxyRequestEvent input, Context context) {
        logger.info(input.toString());
        try {
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            final String id = pathParameters.get("id");
            logger.info("deleting by id:" + id);
            Book book = new Book();
            book.setId(id);
            book = mapper.load(book);
            String status = book.getStatus();
            logger.info("Current book status: " + book.getStatus());
            if("available".equals(status)){
                book.setStatus("removed");
                book.setRemovalDate(ZonedDateTime.now(ZoneId.of("Europe/Warsaw")).toLocalDate().toString());
                mapper.save(book, mapperConfig);
            }
            else{
                logger.info("Book cannot be deleted while being \"" + status + "\"");
                return "Book is currently " + status;
            }
            return null;
        }
        catch(Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during book deletion");
        }
//        try{
//            logger.info("Getting book");
//            final Map<String, String> pathParameters = input.getQueryStringParameters();
//            logger.info("Parameters received: " + pathParameters.toString());
//            final String title = pathParameters.get("title");
//
//            Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
//            eav.put(":title", new AttributeValue().withS(title));
//
//            DynamoDBDeleteExpression deleteExpression = new DynamoDBDeleteExpression()
//                    .withExpectedEntry("title", new ExpectedAttributeValue().withValue(new AttributeValue().withS(title)));
//
//            mapper.delete(Book.class, deleteExpression);
//        }
//        catch(Exception e){
//            throw new GeneralException("Exception when deleting book");
//        }
    }

    //Method: removes all books from database with given exact title
    public String deleteBooks(final APIGatewayProxyRequestEvent input, final Context context){
        logger.info(input.toString());
        try {
            GetBookServiceImplementation getBookServiceImplementation = new GetBookServiceImplementation();
            List<Book> books = getBookServiceImplementation.getBooksByTitle(input, context);

//            final Map<String, String> pathParameters = input.getQueryStringParameters();
//            final String isbn = pathParameters.get("isbn");
//            Book book = new Book();
//            book.setIsbn(isbn);
            String result = null;
            for(Book item : books){
                if("available".equals(item.getStatus())) {
                    item.setStatus("removed");
                    item.setRemovalDate(ZonedDateTime.now(ZoneId.of("Europe/Warsaw")).toLocalDate().toString());
                }
                else{
                    result = result.concat(item.getId() + " ");
                }
            }
            mapper.batchSave(books);
            return result;
        }
        catch(Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during deleting multiple books");
        }
    }
}
