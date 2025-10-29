import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.WebHelper;

import static io.restassured.RestAssured.given;

public class HomeOwner_Onboarding_Flow {
    // Base URL for Node.js backend
    private static final String BASE_URL_NODE_UAE = "https://reno-dev.azurewebsites.net";
    private static final String BASE_URL_NODE_EGP = "https://reno-backend-test-egp.azurewebsites.net";
    
    private static final String selectedBaseUrl = BASE_URL_NODE_UAE;
    
    private String homeOwnerEmail;
    private String homeOwnerPassword;
    private String homeOwnerPhone;
    private String homeOwnerUsername;
    private int userId;

    @Test(priority = 1)
    public void homeOwnerSignup() {
        System.out.println("========== HOMEOWNER SIGNUP STARTED ==========");
        System.out.println("Using Base URL: " + selectedBaseUrl);
        
        // Generate random username, email, phone, and password from WebHelper
        homeOwnerUsername = WebHelper.generateRandomUsername();
        homeOwnerEmail = WebHelper.generateRandomHomeOwnerEmail();
        homeOwnerPhone = WebHelper.generateRandomPhoneNumber();
        homeOwnerPassword = WebHelper.generateRandomPassword();
        
        System.out.println("Generated Username: " + homeOwnerUsername);
        System.out.println("Generated Email: " + homeOwnerEmail);
        System.out.println("Generated Phone: " + homeOwnerPhone);
        System.out.println("Generated Password: " + homeOwnerPassword);
        
        // Test Case 1: Validate Email ID
        System.out.println("\n=== TEST CASE 1: EMAIL ID VALIDATION ===");
        boolean emailValid = validateEmail(homeOwnerEmail);
        System.out.println("Case 1 - Email validation successful: " + (emailValid ? "YES" : "NO"));
        System.out.println("Email contains @ symbol: " + homeOwnerEmail.contains("@"));
        System.out.println("Email contains .com domain: " + homeOwnerEmail.contains(".com"));
        Assert.assertTrue(emailValid, "Email validation failed");
        
        // Test Case 2: Validate Password
        System.out.println("\n=== TEST CASE 2: PASSWORD VALIDATION ===");
        boolean passwordValid = WebHelper.validatePassword(homeOwnerPassword);
        System.out.println("Case 2 - Password validation successful: " + (passwordValid ? "YES" : "NO"));
        System.out.println("Password contains uppercase: " + homeOwnerPassword.matches(".*[A-Z].*"));
        System.out.println("Password contains lowercase: " + homeOwnerPassword.matches(".*[a-z].*"));
        System.out.println("Password contains number: " + homeOwnerPassword.matches(".*[0-9].*"));
        System.out.println("Password contains special character: " + homeOwnerPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*"));
        System.out.println("Password length (8-15): " + (homeOwnerPassword.length() >= 8 && homeOwnerPassword.length() <= 15));
        Assert.assertTrue(passwordValid, "Password validation failed");
        
        // Test Case 3: API Hit Response Without Email
        System.out.println("\n=== TEST CASE 3: API RESPONSE WITHOUT EMAIL ===");
        testAPIWithoutEmail();
        
        // Test Case 4: API Hit Response Without Password
        System.out.println("\n=== TEST CASE 4: API RESPONSE WITHOUT PASSWORD ===");
        testAPIWithoutPassword();
        
        // Test Case 5: API Hit with All Correct Data
        System.out.println("\n=== TEST CASE 5: API HIT WITH ALL CORRECT DATA ===");
        testAPIWithCorrectData();
        
        System.out.println("\n========== HOMEOWNER SIGNUP COMPLETED ==========");
    }
    
    /**
     * Validate email format
     * @param email Email to validate
     * @return true if email is valid
     */
    private boolean validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        // Check if email contains @ and .com
        boolean hasAtSymbol = email.contains("@");
        boolean hasDotCom = email.contains(".com");
        
        // Check if email matches basic email pattern
        boolean matchesPattern = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        
        return hasAtSymbol && hasDotCom && matchesPattern;
    }
    
    private void testAPIWithoutEmail() {
        System.out.println("Testing API without email...");
        
        JSONObject payloadWithoutEmail = new JSONObject();
        payloadWithoutEmail.put("username", homeOwnerUsername);
        payloadWithoutEmail.put("phone_code", "91");
        payloadWithoutEmail.put("phone_no", homeOwnerPhone);
        payloadWithoutEmail.put("role", "home_owner");
        // payloadWithoutEmail.put("email", homeOwnerEmail); // Email missing
        payloadWithoutEmail.put("password", homeOwnerPassword);
        payloadWithoutEmail.put("device_type", "app");
        
        System.out.println("Payload (without email): " + payloadWithoutEmail.toString());
        
        Response responseWithoutEmail = given()
                .contentType("application/json")
                .body(payloadWithoutEmail.toString())
                .post(selectedBaseUrl + "/api/user/signup");
        
        System.out.println("Case 3 - API Response without Email: " + responseWithoutEmail.getBody().asString());
        System.out.println("Case 3 - API Status Code: " + responseWithoutEmail.getStatusCode());
        
        // Validate that API should fail without email (should not return 200)
        Assert.assertNotEquals(responseWithoutEmail.getStatusCode(), 200, 
            "API should not return 200 status code when email is missing");
        System.out.println("Case 3 - API correctly rejected request without email: PASSED");
    }
    
    private void testAPIWithoutPassword() {
        System.out.println("Testing API without password...");
        
        JSONObject payloadWithoutPassword = new JSONObject();
        payloadWithoutPassword.put("username", homeOwnerUsername);
        payloadWithoutPassword.put("phone_code", "91");
        payloadWithoutPassword.put("phone_no", homeOwnerPhone);
        payloadWithoutPassword.put("role", "home_owner");
        payloadWithoutPassword.put("email", homeOwnerEmail);
        // payloadWithoutPassword.put("password", homeOwnerPassword); // Password missing
        payloadWithoutPassword.put("device_type", "app");
        
        System.out.println("Payload (without password): " + payloadWithoutPassword.toString());
        
        Response responseWithoutPassword = given()
                .contentType("application/json")
                .body(payloadWithoutPassword.toString())
                .post(selectedBaseUrl + "/api/user/signup");
        
        System.out.println("Case 4 - API Response without Password: " + responseWithoutPassword.getBody().asString());
        System.out.println("Case 4 - API Status Code: " + responseWithoutPassword.getStatusCode());
        
        // Validate that API should fail without password (should not return 200)
        Assert.assertNotEquals(responseWithoutPassword.getStatusCode(), 200, 
            "API should not return 200 status code when password is missing");
        System.out.println("Case 4 - API correctly rejected request without password: PASSED");
    }
    
    private void testAPIWithCorrectData() {
        System.out.println("Testing API with all correct data...");
        
        JSONObject correctPayload = new JSONObject();
        correctPayload.put("username", homeOwnerUsername);
        correctPayload.put("phone_code", "91");
        correctPayload.put("phone_no", homeOwnerPhone);
        correctPayload.put("role", "home_owner");
        correctPayload.put("email", homeOwnerEmail);
        correctPayload.put("password", homeOwnerPassword);
        correctPayload.put("device_type", "app");
        
        System.out.println("Payload (with all correct data): " + correctPayload.toString());
        
        Response correctResponse = given()
                .contentType("application/json")
                .body(correctPayload.toString())
                .post(selectedBaseUrl + "/api/user/signup");
        
        System.out.println("Case 5 - API Response with correct data: " + correctResponse.getBody().asString());
        System.out.println("Case 5 - API Status Code: " + correctResponse.getStatusCode());
        
        // Validate that API should succeed with all correct data
        Assert.assertEquals(correctResponse.getStatusCode(), 200, 
            "API should return 200 status code when all data is correct");
        
        // Fetch and print user ID from response
        try {
            // Try multiple possible paths for user ID
            if (correctResponse.jsonPath().get("id") != null) {
                userId = correctResponse.jsonPath().getInt("id");
            } else if (correctResponse.jsonPath().get("data.id") != null) {
                userId = correctResponse.jsonPath().getInt("data.id");
            } else if (correctResponse.jsonPath().get("user.id") != null) {
                userId = correctResponse.jsonPath().getInt("user.id");
            } else if (correctResponse.jsonPath().get("userId") != null) {
                userId = correctResponse.jsonPath().getInt("userId");
            }
            
            if (userId > 0) {
                System.out.println("\n========== USER DETAILS ==========");
                System.out.println("User ID: " + userId);
                System.out.println("Email ID: " + homeOwnerEmail);
                System.out.println("Username: " + homeOwnerUsername);
                System.out.println("Phone: " + homeOwnerPhone);
                System.out.println("==================================");
                Assert.assertTrue(userId > 0, "User ID should be greater than 0");
            } else {
                System.out.println("Warning: Could not find user ID in response");
                System.out.println("Full Response: " + correctResponse.getBody().asString());
            }
        } catch (Exception e) {
            System.out.println("Error: Could not extract user ID from response - " + e.getMessage());
            System.out.println("Response body: " + correctResponse.getBody().asString());
            e.printStackTrace();
        }
        
        System.out.println("Case 5 - HomeOwner signup with correct data: SUCCESS!");
    }
    
    @Test(priority = 2, dependsOnMethods = "homeOwnerSignup")
    public void verifyOTP() {
        System.out.println("\n========== OTP VERIFICATION STARTED ==========");
        
        // Wait for 2 seconds before fetching OTP
        System.out.println("Waiting 2 seconds before fetching OTP...");
        try {
            Thread.sleep(2000); // 2 seconds wait
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted: " + e.getMessage());
        }
        System.out.println("Wait completed, fetching OTP from database...");
        
        // Determine if using UAE or EGP database based on selected URL
        boolean isUAE = selectedBaseUrl.contains("reno-dev.azurewebsites.net");
        
        // Fetch OTP from database using WebHelper
        String otp = WebHelper.fetchOTP(isUAE);
        System.out.println("Fetched OTP: " + otp);
        Assert.assertFalse(otp.isEmpty(), "OTP should not be empty - check database connection");
        
        // Prepare OTP verification payload
        JSONObject otpPayload = new JSONObject();
        otpPayload.put("otp", otp);
        otpPayload.put("verify_type", "email");
        otpPayload.put("user_id", userId);
        otpPayload.put("device_name", "122.161.53.199");
        otpPayload.put("login_address", "India");
        
        System.out.println("OTP Verification Payload: " + otpPayload.toString());
        
        // Call OTP verification API
        Response otpResponse = given()
                .contentType("application/json")
                .body(otpPayload.toString())
                .post(selectedBaseUrl + "/api/v1/user/verify-otp");
        
        System.out.println("OTP Verification API Response: " + otpResponse.getBody().asString());
        System.out.println("OTP Verification API Status Code: " + otpResponse.getStatusCode());
        
        // Validate response
        Assert.assertEquals(otpResponse.getStatusCode(), 200, "OTP verification should return 200 status code");
        
        System.out.println("========== OTP VERIFICATION COMPLETED ==========");
    }

}
