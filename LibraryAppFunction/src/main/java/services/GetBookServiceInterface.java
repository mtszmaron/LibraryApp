package services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.Book;
import dao.History;

import java.util.List;

public interface GetBookServiceInterface {
    public default Book getBook(APIGatewayProxyRequestEvent input, Context context){
        return null;
    }
    public default List<Book> getBooksByTitle(final APIGatewayProxyRequestEvent input, final Context context){
        return null;
    }
    public default List<Book> getBooksByCategory(final APIGatewayProxyRequestEvent input, final Context context){
        return null;
    }
    public default List<History> getBookHistory(final APIGatewayProxyRequestEvent input, final Context context){
        return null;
    }
}
