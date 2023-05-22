package services.implementations;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.BookingQueueServiceInterface;
import util.GeneralException;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;

import static com.amazonaws.services.sqs.model.QueueAttributeName.ApproximateNumberOfMessages;

public class BookingQueueServiceImplementation implements BookingQueueServiceInterface {


    private static final Logger logger = LoggerFactory.getLogger(BookingQueueServiceImplementation.class);
    private static final String URL = "https://ba528c66-d4d0-4290-978a-010967193123.mock.pstmn.io/test2";

    @Override
    public void sendMessages(SQSEvent.SQSMessage message) {
        try{
            URL obj = new URL(URL);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Content-Type", "application/json");

            //String urlParameters = "{\"message\": \"" + message.getBody() + "\"}";
            String urlParameters = message.getBody();

            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(urlParameters.getBytes());
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();
            logger.info("GET Response Code : " + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                logger.info(response.toString());
            } else {
                logger.info("POST request did not work.");
                throw new RuntimeException();
            }
        }
        catch (Exception e){
            throw new GeneralException("Error while connecting to host");
        }
    }

//    private static final String USER_AGENT = "Mozilla/5.0";
//
//    private static final String GET_URL = "https://0a89ac9e-222c-4c0d-afb0-36bb7aaaea9c.mock.pstmn.io/test";
//
//    @Override
//    public String receiveAndSendMessages(SQSEvent input, Context context) {
//        logger.info("getting request... URL: " + GET_URL);
//        try{
//            URL obj = new URL(GET_URL);
//            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//            con.setRequestMethod("GET");
//            con.setRequestProperty("User-Agent", USER_AGENT);
//            int responseCode = con.getResponseCode();
//            System.out.println("GET Response Code :: " + responseCode);
//            if (responseCode == HttpURLConnection.HTTP_OK) { // success
//                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//                String inputLine;
//                StringBuffer response = new StringBuffer();
//
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//
//                // print result
//                System.out.println(response.toString());
//            } else {
//                System.out.println("GET request did not work. Response code: " + responseCode);
//            }
//        }
//        catch (Exception e){
//            logger.info(e.toString());
//        }
//        return "elo";
//    }
}
