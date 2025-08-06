package utils;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class AuthHelper {
    private static String contractorToken;
    private static String homeOwnerToken;
    private static String adminToken;

    // Public method to fetch Contractor Token
    public static String getContractorToken() {
        if (contractorToken == null) {
            contractorToken = fetchToken("sarthak.bansal@renohome.ae", "SB@123sarthak", "web");
            System.out.println("Generated Contractor Token: " + contractorToken);
        }
        return contractorToken;
    }

    // Public method to fetch Admin Token
    public static String getAdminToken() {
        if (adminToken == null) {
            adminToken = fetchToken("sarthak.bansal@renohome.ae", "Demo@123", "reno");
            System.out.println("Generated Admin Token: " + adminToken);
        }
        return adminToken;
    }

    // Public method to fetch HomeOwner Token
    public static String getHomeOwnerToken() {
        if (homeOwnerToken == null) {
            homeOwnerToken = fetchToken("sarthak.bansal@renohome.ae", "Sarthak@123", "app");
            System.out.println("Generated HomeOwner Token: " + homeOwnerToken);
        }
        return homeOwnerToken;
    }

    // Private helper method to fetch token
    private static String fetchToken(String email, String password, String deviceType) {
        String requestBody = String.format(
                "{\"email\": \"%s\", \"password\": \"%s\", \"device_type\": \"%s\"}",
                email, password, deviceType
        );

        try {
            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .post("https://reno-dev.azurewebsites.net/api/user/login");

            int statusCode = response.getStatusCode();
            String responseBody = response.getBody().asString();

            if (statusCode != 200) {
                System.err.println("Login API failed with status: " + statusCode);
                System.err.println("Response body: " + responseBody);
                throw new RuntimeException("Failed to fetch token. Status: " + statusCode);
            }

            String token = response.jsonPath().getString("token");
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Token not found in response.");
            }

            return token;

        } catch (Exception e) {
            throw new RuntimeException("Exception while fetching token: " + e.getMessage(), e);
        }
    }
}
