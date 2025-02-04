
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.AuthHelper;

import static io.restassured.RestAssured.given;


public class APITest {
    @Test
    public void testGetAPI() {
        // Get token dynamically
        String token = AuthHelper.getAuthToken();

        // Debug: Print the token
        System.out.println("Generated Token In APITest : " + token);

        // Make API request
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json") // Ensure JSON response
                .contentType(ContentType.JSON)
                .get("https://reno-dev.azurewebsites.net/api/contractor/list-contractors");

        // Debug: Print full response
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Validate response
        Assert.assertEquals(response.getStatusCode(), 200);
    }

}
