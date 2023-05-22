package services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.History;

import java.util.List;

public interface GetHistoryServiceInterface {

    public default List<History> getBookHistory(final APIGatewayProxyRequestEvent input, final Context context) { return null; }
    public default List<History> getPersonHistory(final APIGatewayProxyRequestEvent input, final Context context) { return null; }
}
