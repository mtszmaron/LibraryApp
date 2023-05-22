package services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.Book;

public interface UpdateBookServiceInterface {
    public default Book updateBook(final APIGatewayProxyRequestEvent input, final Context context){
        return null;
    }
    public default Book updateBookStatus(APIGatewayProxyRequestEvent input, Context context){ return null; }

}
