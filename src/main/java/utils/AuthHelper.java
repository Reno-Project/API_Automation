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

    // Public method to fetch HomeOwner Token
    public static String getHomeOwnerToken() {
        if (homeOwnerToken == null) {
            homeOwnerToken = fetchToken("sarthak.bansal@renohome.ae", "Sarthak@123", "app");
            System.out.println("Generated HomeOwner Token: " + homeOwnerToken);
        }
        return homeOwnerToken;
    }

    // Public method to fetch Admin Token
    public static String getAdminToken() {
        if (adminToken == null) {
            adminToken = fetchToken("sarthak.bansal@renohome.ae", "Demo@123", "reno");
            System.out.println("Generated Admin Token: " + adminToken);
        }
        return adminToken;
    }

    // Private helper method to fetch token
    private static String fetchToken(String email, String password, String deviceType) {
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"device_type\": \"" + deviceType + "\"}")
                .post("https://reno-dev.azurewebsites.net/api/user/login");

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to get token for " + email + ": " + response.getStatusCode() + " - " + response.getBody().asString());
        }

        return response.jsonPath().getString("token");
    }
}
