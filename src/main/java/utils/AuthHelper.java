package utils;
import io.restassured.response.Response;
import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;

public class AuthHelper {
    private static String token;

    // Method to fetch and return the token
    public static String getAuthToken() {
        // Token For Contractor Portal
        if (token == null) {  // Fetch token only if not already stored
            Response response = given()
                    .contentType(ContentType.JSON)
                    .body("{\"email\": \"sarthak.bansal@renohome.ae\", \"password\": \"SB@123sarthak\",\"device_type\": \"web\"}")
                    .post("https://reno-dev.azurewebsites.net/api/user/login");

            if (response.getStatusCode() != 200) {
                throw new RuntimeException("Failed to get token: " + response.getStatusCode() + " - " + response.getBody().asString());
            }

            // Assign the extracted token to the class-level variable
            token = response.jsonPath().getString("token");

            // Print the token for debugging
            System.out.println("Generated Token In AuthHelper: " + token);
        }

        return token;
    }
}
