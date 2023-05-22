import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.Book;
import dao.History;
import dao.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.implementations.*;
import util.GeneralException;

/**
 * Handler for requests to Lambda function.
 */
public class LibraryAppHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LibraryAppHandler.class);

    /*
    Interface method handling request.
    First switch checks for type of httpMethod.
    Second switch checks for type of object to perform operations.
     */
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        String httpMethod = input.getHttpMethod();
        Map<String, String> pathParameters = input.getQueryStringParameters();
        String output = "";
        int statusCode = 200;

        logger.info("Called path: " + input.getResource());

        try {
            if (pathParameters == null) {
                output = String.format("{\"Error\": \"No path parameters found in query.\"}");

            } else {
                logger.info("HTTP Method invoked: " + httpMethod);
                logger.info("Type received: " + pathParameters.get("type"));
                ObjectMapper objectMapper = new ObjectMapper();
                switch (httpMethod) {
                    case "GET":
                        switch (pathParameters.get("type")) {
                            case "book": {
                                GetBookServiceImplementation getBookServiceImplementation = new GetBookServiceImplementation();
                                if (pathParameters.containsKey("id")) {
                                    Book foundBook = getBookServiceImplementation.getBook(input, context);
                                    if(foundBook == null){
                                        output = "{\"message\": \"No book found with given id.\"}";
                                        statusCode = 404;
                                    }
                                    else {
                                        logger.info("Found book: " + foundBook.getId());
                                        String jsonBook = objectMapper.writeValueAsString(foundBook);
                                        output = String.format(jsonBook + "\n");
                                    }
                                } else if (pathParameters.containsKey("title")) {
                                    logger.info("Getting books by title: " + input.getQueryStringParameters().get("title"));
                                    List<Book> foundBooks = getBookServiceImplementation.getBooksByTitle(input, context);

                                    if(foundBooks == null){
                                        output = "{\"message\": \"No books found with given title.\"}";
                                        statusCode = 404;
                                    }
                                    else {
                                        output = String.format("\"found_records\": ");
                                        for (Book books : foundBooks) {
                                            output = output.concat(books + "\n");
                                        }
                                    }
                                } else if (pathParameters.containsKey("category")) {
                                    List<Book> foundBooks = getBookServiceImplementation.getBooksByCategory(input, context);

                                    if(foundBooks == null) {
                                        output = "{\"message\": \"No books found with given category.\"}";
                                        statusCode = 404;
                                    }
                                    else{
                                        output = String.format("\"found_records\": ");
                                        for (Book books : foundBooks) {
                                            output = output.concat(books + "\n");
                                        }
                                    }
                                } else if (pathParameters.containsKey("months")) {
                                    List<Book> foundBooks = getBookServiceImplementation.getTrendingBooks(input, context);

                                    if(foundBooks == null) {
                                        output = "{\"message\": \"No books found within given time range.\"}";
                                        statusCode = 404;
                                    }
                                    else {
                                        output = String.format("\"found_records\": ");
                                        for (Book books : foundBooks) {
                                            output = output.concat(books + "\n");
                                        }
                                    }
//                                    } catch (NullPointerException e) {
//                                        output = "No books in that time period";
//                                    }
                                }
                                break;
                            }
                            case "person": {
                                logger.info("Request headers: " + pathParameters);
                                if (pathParameters.containsKey("name")) {
                                    GetPersonServiceImplementation getPersonServiceImplementation = new GetPersonServiceImplementation();
                                    List<Person> peopleList = getPersonServiceImplementation.getPersonByName(input, context);

                                    if(peopleList == null) {
                                        output = "{\"message\": \"No people found with given name.\"}";
                                        statusCode = 404;
                                    }
                                    else {
                                        for (Person person : peopleList) {
                                            output = output.concat(person + "\n");
                                        }
                                    }
                                } else {
                                    GetPersonServiceImplementation getPersonServiceImplementation = new GetPersonServiceImplementation();
                                    List<Person> peopleList = getPersonServiceImplementation.getAllPeople(input, context);

                                    if(peopleList == null) {
                                        output = "{\"message\": \"Database has no person records.\"}";
                                        statusCode = 404;
                                    }
                                    else {
                                        output = String.format("\"found_people\": ");
                                        for (Person person : peopleList) {
                                            output = output.concat(person + "\n");
                                        }
                                    }
                                }
                                break;
                            }
                            case "history": {
                                if (pathParameters.containsKey("bookId")) {
                                    GetHistoryServiceImplementation getHistoryServiceImplementation = new GetHistoryServiceImplementation();
                                    List<History> historyList = getHistoryServiceImplementation.getBookHistory(input, context);

                                    if(historyList == null) {
                                        output = "{\"message\": \"Book has no renting history or does not exist.\"}";
                                        statusCode = 404;
                                    }
                                    else {
                                        output = String.format("\"found_history_records\": ");
                                        for (History history : historyList) {
                                            output = output.concat(history + "\n");
                                        }
                                    }
                                } else if (pathParameters.containsKey("personId")) {
                                    GetPersonServiceImplementation getPersonServiceImplementation = new GetPersonServiceImplementation();
                                    Person person = getPersonServiceImplementation.getPersonById(input, context);

                                    if(person == null) {
                                        output = "{\"message\": \"Person with given id does not exist.\"}";
                                        statusCode = 404;
                                    }
                                    else {
                                        output = String.format("\"person_details\": " + person.getName() + " " + person.getSecondName() + " " + person.getLastName() + " " + person.getEmail() + "\n");

                                        GetHistoryServiceImplementation getHistoryServiceImplementation = new GetHistoryServiceImplementation();
                                        List<History> historyList = getHistoryServiceImplementation.getPersonHistory(input, context);
                                        for (History history : historyList) {
                                            output += String.format(history + "\n");
                                        }
                                    }
                                }
                                break;
                            }
                        }
                        break;
                    case "POST":
                        switch (pathParameters.get("type")) {
                            case "book":
                                logger.info("Adding book");
                                AddBookServiceImplementation addBookServiceImplementation = new AddBookServiceImplementation();
                                Book newBook = addBookServiceImplementation.addBook(input, context);

                                logger.info("Added book: " + newBook.getId());

                                String jsonBook = objectMapper.writeValueAsString(newBook);

                                output = String.format(jsonBook + "\n");
                                statusCode = 201;
                                break;
                            case "person":
                                logger.info("Adding person");
                                AddPersonServiceImplementation addPersonServiceImplementation = new AddPersonServiceImplementation();
                                Person newPerson = addPersonServiceImplementation.addPerson(input, context);
                                logger.info("Added person: " + newPerson.getId());
                                output = String.format(newPerson + "\n");
                                statusCode = 201;
                                break;
                        }
                        break;
                    case "DELETE":
                        switch (pathParameters.get("type")) {
                            case "book":
                                logger.info("Deleting book");
                                DeleteBookServiceImplementation deleteBookServiceImplementation = new DeleteBookServiceImplementation();
                                String result = null;
                                if (pathParameters.containsKey("id")) {
                                    result = deleteBookServiceImplementation.deleteBook(input, context);
                                    if(result != null){
                                        output = "{\"message\": \"" + result + "\"}";
                                        statusCode = 403;
                                    }
                                } else if (pathParameters.containsKey("title")) {
                                    result = deleteBookServiceImplementation.deleteBooks(input, context);
                                    if(result != null) output = "{\"message\": \"Books that could not be removed: " + result + "\"}";
                                } else {
                                    output = String.format(" { \"message\": \"Missing identifier\"}");
                                    logger.error("Missing identifier");
                                    statusCode = 400;
                                }
                                break;
                            case "person":
                                logger.info("Deleting person");
                                DeletePersonServiceImplementation deletePersonServiceImplementation = new DeletePersonServiceImplementation();
                                if (pathParameters.containsKey("id")) {
                                    deletePersonServiceImplementation.deletePerson(input, context);
                                } else {
                                    output = String.format(" { \"message\": \"Missing identifier\"}");
                                    logger.error("Missing identifier");
                                    statusCode = 400;
                                }
                                break;
                        }
                        break;
                    case "PUT":
                        UpdateBookServiceImplementation updateBookServiceImplementation = new UpdateBookServiceImplementation();
                        logger.info("Updating object of id: " + input.getQueryStringParameters().get("id"));
                        if (pathParameters.get("id") == null) {
                            output = "Id cannot be empty";
                            statusCode = 400;
                        }
                        else {
                            Book updateBook;
                            if (pathParameters.containsKey("status")) {
                                updateBook = updateBookServiceImplementation.updateBookStatus(input, context);
                            } else {
                                updateBook = updateBookServiceImplementation.updateBook(input, context);
                            }

                            logger.info("Updated Book: " + updateBook.getId());

                            output = String.format(updateBook + "\n");
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        catch(GeneralException | JsonProcessingException e){
            logger.info("Error: " + e + " \n" + e);
            output = e.toString();
            statusCode = 500;
        }
        catch(Exception e){
            logger.info("Error: " + e + " \n" + e);
            output = "Unexpected error";
            statusCode = 500;
        }



        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        return response
                .withStatusCode(statusCode)
                .withBody(output);
    }


}
