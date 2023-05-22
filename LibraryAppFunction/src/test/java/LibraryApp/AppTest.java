package LibraryApp;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.HashMap;

public class AppTest {
//  @Test
//  public void successfulResponse() {
//    App app = new App();
//    APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
//    HashMap<String, String> input = new HashMap<>();
//    input.put("testInput", "tEst 123 cloudwatch");
//    request.setPathParameters(input);
//    APIGatewayProxyResponseEvent result = app.handleRequest(request, null);
//    assertEquals(200, result.getStatusCode().intValue());
//    assertEquals("application/json", result.getHeaders().get("Content-Type"));
//    String content = result.getBody();
//    assertNotNull(content);
//    assertTrue(content.contains("\"message\""));
//    assertTrue(content.contains("\"Library App RG\""));
//    assertTrue(content.contains("\"location\""));
//  }
}
