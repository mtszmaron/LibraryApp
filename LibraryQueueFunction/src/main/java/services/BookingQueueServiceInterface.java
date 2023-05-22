package services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

public interface BookingQueueServiceInterface {

    public default void sendMessages(SQSEvent.SQSMessage message){

    }

    public default String receiveAndSendMessages(final SQSEvent input, final Context context){
        return null;
    }
}
