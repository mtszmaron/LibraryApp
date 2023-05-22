package services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.Book;

public interface AddBookServiceInterface {
    public default Book addBook(final APIGatewayProxyRequestEvent input, final Context context){
        return null;
    }
}
