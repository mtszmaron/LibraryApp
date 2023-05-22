package services.implementations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.Book;
import dao.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.GetBookServiceInterface;
import util.GeneralException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

//Implementation of interface in charge of retrieving books from database
public class GetBookServiceImplementation implements GetBookServiceInterface {
    private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private DynamoDBMapper mapper = new DynamoDBMapper(client);

    private static final Logger logger = LoggerFactory.getLogger(GetBookServiceImplementation.class);

    //Method: getting single book by given Id
    public Book getBook(APIGatewayProxyRequestEvent input, Context context){
        logger.info(input.toString());
        try{
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            final String id = pathParameters.get("id");
            Book book = new Book();
            book.setId(id);
            logger.info("book object: id: " + book.getId());// + ", type: " + book.getType());
            book = mapper.load(book);
            return book;
        }
        catch (Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during book getting");
        }
    }

    //Method: getting books with same, exact title as given in request
    public List<Book> getBooksByTitle(final APIGatewayProxyRequestEvent input, final Context context){
        try {
            logger.info("Getting book");
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            logger.info("Parameters received: " + pathParameters.toString());
            final String title = pathParameters.get("title");

//            Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
//            eav.put(":title", new AttributeValue().withS(title));

//            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
//                    .withFilterExpression("title = :title")
//                    .withExpressionAttributeValues(eav);
//            logger.info("Scan expr: " + scanExpression.getFilterExpression() + "\nScan vals:" + scanExpression.getExpressionAttributeValues());
//            List<Book> foundBooks = mapper.scan(Book.class, scanExpression);

//            Map<String, AttributeValue> eav = new HashMap<>();
//            eav.put(":val1", new AttributeValue().withS("book"));
//
//            final HashMap<String, String> ean = new HashMap<>();
//            ean.put("#type", "type");
            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":val1", new AttributeValue().withS("book"));
            eav.put(":val2", new AttributeValue().withS("removed"));

            final HashMap<String, String> ean = new HashMap<>();
            ean.put("#type", "type");
            ean.put("#status", "status");


            Book book = new Book();
            book.setTitle(title);
            DynamoDBQueryExpression<Book> queryExpression = new DynamoDBQueryExpression<Book>()
                    .withIndexName("titleIndex")
                    .withFilterExpression("#type = :val1 and #status <> :val2")
                    .withExpressionAttributeNames(ean)
                    .withExpressionAttributeValues(eav)
                    .withHashKeyValues(book)
                    .withConsistentRead(false);
            List<Book> foundBooks = mapper.query(Book.class,queryExpression);

            logger.info("type of list: " + foundBooks.getClass().getName() + " // " + foundBooks.getClass());

            for(Book books : foundBooks){
                logger.info("Found book: " + books.getTitle() + " type: " + books.getItemType());
            }
            return foundBooks;
        }
        catch (Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during getting books by title");
        }
    }

    //Method: finding all books with specified category
    public List<Book> getBooksByCategory(final APIGatewayProxyRequestEvent input, final Context context){
        try{
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            final String category = pathParameters.get("category");

            Book book = new Book();
            book.setCategory(category);
            DynamoDBQueryExpression<Book> queryExpression = new DynamoDBQueryExpression<Book>()
                    .withIndexName("categoryIndex")
                    .withHashKeyValues(book)
                    .withConsistentRead(false);
            List<Book> BookList = mapper.query(Book.class,queryExpression);

            return BookList;
        }
        catch (Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during getting books by category");
        }
    }

    //Method: gets all books from database and returns top 5 books with most rents during x last months, where x is input parameter "months"
    public List<Book> getTrendingBooks(final APIGatewayProxyRequestEvent input, final Context context){
        try {
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            final String months = pathParameters.get("months");
            final int numberOfItemsToGet = 5;

            String returnDate = ZonedDateTime.now(ZoneId.of("Europe/Warsaw")).minusMonths(Integer.parseInt(months)).toLocalDate().toString();

            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":val1", new AttributeValue().withS("history"));
            eav.put(":val2", new AttributeValue().withS(returnDate));

            final HashMap<String, String> ean = new HashMap<>();
            ean.put("#type", "type");

            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                    .withFilterExpression("#type = :val1 and rentDate >= :val2")
                    .withExpressionAttributeNames(ean)
                    .withExpressionAttributeValues(eav);
            List<History> historyList = mapper.scan(History.class, scanExpression);

            // Looping through items, counting them and finally sorting
            Map<String, Integer> hm = new HashMap<String, Integer>();

            for (History history : historyList) {
                Integer c = hm.get(history.getBookId());
                hm.put(history.getBookId(), (c == null) ? 1 : c + 1);
            }

            List<Map.Entry<String, Integer>> nlist = new ArrayList<>(hm.entrySet());
            nlist.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            ArrayList<Book> bookList = new ArrayList<>();

            for (int i = 0; i < numberOfItemsToGet && i < nlist.size(); i++) {
                Book book = new Book();
                book.setId(nlist.get(i).getKey());
                bookList.add(book);
            }

            Map<String, List<Object>> batchResults = mapper.batchLoad(bookList);

            List<Object> books = batchResults.get("LibraryTable");
            @SuppressWarnings("unchecked")
            List<Book> newList = (List<Book>) (List<?>) books;

            bookList.replaceAll(old -> newList.stream().filter(updated -> updated.getId().equals(old.getId())).findFirst().orElse(old));

            return bookList;
        }
        catch (Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during getting trending books");
        }
    }


//    public List<History> getBookHistory(final APIGatewayProxyRequestEvent input, final Context context){
//        final Map<String, String> pathParameters = input.getQueryStringParameters();
//        final String id = pathParameters.get("bookId");
//
//        logger.info("Getting book's history of id: " + id);
//        History history = new History();
//        history.setBookId(id);
//        DynamoDBQueryExpression<History> queryExpression = new DynamoDBQueryExpression<History>()
//                .withIndexName("bookIdIndex")
//                .withHashKeyValues(history)
//                .withConsistentRead(false);
//        List<History> bookHistory = mapper.query(History.class, queryExpression);
//
//        return bookHistory;
//    }
}
