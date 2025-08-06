package utils;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class AuthHelper {
    private static String contractorToken;
    private static String homeOwnerToken;
    private static String adminToken;
    private static Map<String, String> userTokens = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);

    // Public method to fetch Contractor Token (unchanged)
    public static String getContractorToken() {
        if (contractorToken == null) {
            contractorToken = fetchToken("sarthak.bansal@renohome.ae", "SB@123sarthak", "web");
            System.out.println("Generated Contractor Token: " + contractorToken);
        }
        return contractorToken;
    }

    // Public method to fetch Admin Token (unchanged)
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

    // Method to get HomeOwner token with specific credentials
    public static String getHomeOwnerToken(String email, String password) {
        String userKey = email + "_homeowner";
        if (!userTokens.containsKey(userKey)) {
            String token = fetchToken(email, password, "app");
            userTokens.put(userKey, token);
            System.out.println("Generated HomeOwner Token for " + email + ": " + token);
        }
        return userTokens.get(userKey);
    }

    // Method to clear cached tokens (useful for testing different users)
    public static void clearCachedTokens() {
        userTokens.clear();
        homeOwnerToken = null;
        System.out.println("Cached tokens cleared successfully.");
    }

    // Method to get token for any user type
    public static String getUserToken(String email, String password, String userType) {
        String userKey = email + "_" + userType;
        if (!userTokens.containsKey(userKey)) {
            String deviceType = "app"; // default
            if ("contractor".equals(userType)) {
                deviceType = "web";
            } else if ("admin".equals(userType)) {
                deviceType = "reno";
            }
            
            String token = fetchToken(email, password, deviceType);
            userTokens.put(userKey, token);
            System.out.println("Generated " + userType + " Token for " + email + ": " + token);
        }
        return userTokens.get(userKey);
    }

    // Private helper method to fetch token (unchanged)
    private static String fetchToken(String email, String password, String deviceType) {
        // Optional: log payload for debugging
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
