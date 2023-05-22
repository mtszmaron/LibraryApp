package services.implementations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import dao.Book;
import dao.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.AddBookServiceInterface;
import util.GeneralException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

//Implementation of interface in charge of updating books in database and creating history of borrows
public class UpdateBookServiceImplementation {
    private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private DynamoDBMapper mapper = new DynamoDBMapper(client);
    DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig.Builder()
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .build();

    private static final Logger logger = LoggerFactory.getLogger(UpdateBookServiceImplementation.class);

    //Method: changing title and category of book specified by given id
    public Book updateBook(APIGatewayProxyRequestEvent input, Context context){
        logger.info(input.toString());
        try{
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            final String id = pathParameters.get("id");
            final String newTitle = pathParameters.get("newTitle");
            final String newCategory = pathParameters.get("newCategory");
            logger.info("Updating book id: " + id);
            Book book = new Book();
            book.setId(id);
            book.setTitle(newTitle);
            book.setCategory(newCategory);
            mapper.save(book, mapperConfig);
            logger.info("Updated book successfuly");
            return book;
        }
        catch (Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during changing book's information");
        }
    }

    //Method: updating book's status based on its current status and new status, creates and inserts into database history object upon return
    public Book updateBookStatus(APIGatewayProxyRequestEvent input, Context context){
        try{
            final Map<String, String> pathParameters = input.getQueryStringParameters();
            final String id = pathParameters.get("id");
            final String newStatus = pathParameters.get("status");
            Book book = new Book();
            book.setId(id);
            logger.info("Changing status");
            book = mapper.load(book);
            logger.info("Book loaded: " + book.getId() + " " + book.getTitle());
            switch (book.getStatus()){
                case "available":
                    if("rented".equals(newStatus)) {
                        String personId = pathParameters.get("personId");
                        logger.info("Setting book to rented");
                        if (book.getBookedBy() == null || book.getBookedBy().equals(personId)){
                            book.setStatus(newStatus);
                            book.setRentDate(ZonedDateTime.now(ZoneId.of("Europe/Warsaw")).toLocalDate().toString());
                            book.setRentPeriod(28);
                            book.setRentedBy(personId);
                            mapper.save(book, mapperConfig);
                        }
                        else{
                            logger.info("Book cannot be rented");
                        }
                    }
//                    else if(newStatus.equals("booking")){
//                        String personId = pathParameters.get("personId");
//                        logger.info("Setting booking");
//                        book.setBookedBy(personId);
//                        mapper.save(book, mapperConfig);
//                    }
                    else{
                        logger.info("Book cannot be set to status: \"" + newStatus + "\" while being \"" + book.getStatus() + "\"");
                    }
                    break;
                case "rented":
                    if(pathParameters.containsKey("personId") && book.getBookedBy() == null){
                        String personId = pathParameters.get("personId");
                        book.setBookedBy(personId);
                        mapper.save(book);
                    }
                    else{
                        logger.info("Book is already booked");
                    }

                    if("available".equals(newStatus)){
                        History history;
                        if(book.getBookedBy() == null) {
                            logger.info("Setting book to available");
                            history = new History(book);
                            book.setStatus(newStatus);
//                            returnBook(book);
//                            mapper.save(book);
//                            mapper.save(history);
                        }
                        else{
                            logger.info("Setting book to booking");
                            history = new History(book);
                            book.setStatus("booking");

                            final String QUEUE_NAME = "LibraryQueue";
                            logger.info("Queue name: " + QUEUE_NAME);
                            final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

                            String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
                            //queueUrl = "https://sqs.eu-north-1.amazonaws.com/006912726820/DemoStandardQueue";
                            String messageBody = "{\"bookId\": \"" + book.getId() + "\", \"userId\": \"" + book.getBookedBy() + "\"}";
                            SendMessageRequest send_msg_request = new SendMessageRequest()
                                    .withQueueUrl(queueUrl)
                                    .withMessageBody(messageBody)
                                    .withDelaySeconds(5);
                            sqs.sendMessage(send_msg_request);
                            book.setBookingEndDate(ZonedDateTime.now(ZoneId.of("Europe/Warsaw")).plusDays(2).toLocalDate().toString());
//                            returnBook(book);
//                            mapper.save(book);
//                            mapper.save(history);
                        }
                        returnBook(book);
                        mapper.save(book);
                        mapper.save(history);
                    }
                    else{
                        logger.info("Book cannot be set to status: \"" + newStatus + "\" while being \"" + book.getStatus() + "\"");
                    }
                    break;
                case "booking":
                    if("rented".equals(newStatus)){
                        String personId = pathParameters.get("personId");
                        if(book.getBookedBy().equals(personId)){
                            logger.info("Renting to reservation owner");
                            book.setBookedBy(null);
                            book.setBookingEndDate(null);
                            book.setStatus(newStatus);
                            book.setRentedBy(personId);
                            book.setRentPeriod(28);
                            book.setRentDate(ZonedDateTime.now(ZoneId.of("Europe/Warsaw")).toLocalDate().toString());
                        }
                        else if(book.getBookingEndDate().equals(ZonedDateTime.now(ZoneId.of("Europe/Warsaw")).toLocalDate().toString())){
                            logger.info("Reservation is outdated, renting book to specified person");
                            book.setBookedBy(null);
                            book.setBookingEndDate(null);
                            book.setStatus(newStatus);
                            book.setRentedBy(personId);
                            book.setRentPeriod(28);
                            book.setRentDate(ZonedDateTime.now(ZoneId.of("Europe/Warsaw")).toLocalDate().toString());
                        }
                        else{
                            logger.info("Specified person is not owner of reservation");
                        }
                    }
//                    else if("available".equals(newStatus)){
//                        logger.info("Setting book to available");
//                        if() {
//                            book.setStatus(newStatus);
//                            History history = new History(book);
//                            returnBook(book);
//                            //message person that booked it
//                            mapper.save(book);
//                            mapper.save(history);
//                        }
//                    }
                    else{
                        logger.info("Book cannot be set to status: \"" + newStatus + "\" while being \"" + book.getStatus() + "\"");
                    }
                    break;
            }
            mapper.save(book);
            logger.info("Status changed");
            return book;
        }
        catch (Exception e){
            logger.info(e.toString());
            throw new GeneralException("Error during updating book's status");
        }
    }

    //Method: helping method to reset rent date, period and renting person fields after history object is created
    private void returnBook(Book book){
        logger.info("Returning book with id: " + book.getId());
        book.setRentDate(null);
        book.setRentPeriod(0);
        book.setRentedBy(null);
    }


}
