import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.implementations.BookingQueueServiceImplementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingQueueHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

    private static final Logger logger = LoggerFactory.getLogger(BookingQueueHandler.class);

    @Override
    public SQSBatchResponse  handleRequest(final SQSEvent sqsEvent, final Context context) {


        List<SQSBatchResponse.BatchItemFailure> batchItemFailureList = new ArrayList<>();
        String messageId = "";
        BookingQueueServiceImplementation bookingQueueServiceImplementation = new BookingQueueServiceImplementation();

        for(SQSEvent.SQSMessage message: sqsEvent.getRecords()){
            try {
                bookingQueueServiceImplementation.sendMessages(message);
            }
            catch(Exception e){
                logger.error("Error: ", e);
                batchItemFailureList.add(new SQSBatchResponse.BatchItemFailure(messageId));
            }
        }
        logger.info(batchItemFailureList.toString());
        return new SQSBatchResponse(batchItemFailureList);

    }
}
