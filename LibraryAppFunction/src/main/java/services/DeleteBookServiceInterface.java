package services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.Book;

public interface DeleteBookServiceInterface {
    public default String deleteBook(final APIGatewayProxyRequestEvent input, final Context context) {
        return null;
    }

    public default String deleteBooks(final APIGatewayProxyRequestEvent input, final Context context){
        return null;
    }
}
