import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.AuthHelper;
import utils.WebHelper;


import static io.restassured.RestAssured.given;

public class Contractor_Onboarding_Flow {

    // Base URL for Node.js backend
    private static final String BASE_URL_NODE_UAE = "https://reno-dev.azurewebsites.net";
    private static final String BASE_URL_NODE_EGP = "https://reno-backend-test-egp.azurewebsites.net";
    
    private static final String selectedBaseUrl = BASE_URL_NODE_UAE;
    private String contractorEmail;
    private String contractorPassword;
    private String contractorPhone;
    private String contractorFirstName;
    private String contractorLastName;

    @Test(priority = 1)
    public void contractorSignup() {
        System.out.println("========== CONTRACTOR SIGNUP STARTED ==========");
        System.out.println("Using Base URL: " + selectedBaseUrl);
        
        // Generate random email, phone, password, first name and last name for this test run
        contractorEmail = WebHelper.generateRandomContractorEmail();
        contractorPhone = WebHelper.generateRandomPhoneNumber();
        contractorPassword = WebHelper.generateRandomPassword();
        contractorFirstName = WebHelper.generateFirstName();
        contractorLastName = WebHelper.generateLastName();
        
        System.out.println("Generated Email: " + contractorEmail);
        System.out.println("Generated Phone: " + contractorPhone);
        System.out.println("Generated Password: " + contractorPassword);
        System.out.println("Generated First Name: " + contractorFirstName);
        System.out.println("Generated Last Name: " + contractorLastName);
        
        // Test Case 1: Password Creation Validation
        System.out.println("\n=== TEST CASE 1: PASSWORD CREATION VALIDATION ===");
        boolean passwordValid = WebHelper.validatePassword(contractorPassword);
        System.out.println("Case 1 - Password creation successful: " + (passwordValid ? "YES" : "NO"));
        
        // Test Case 2: Password Contains Uppercase Letter
        System.out.println("\n=== TEST CASE 2: PASSWORD UPPERCASE VALIDATION ===");
        boolean hasUppercase = contractorPassword.matches(".*[A-Z].*");
        System.out.println("Case 2 - Password contains uppercase letter: " + (hasUppercase ? "YES" : "NO"));
        
        // Test Case 3: Password Contains Lowercase Letter
        System.out.println("\n=== TEST CASE 3: PASSWORD LOWERCASE VALIDATION ===");
        boolean hasLowercase = contractorPassword.matches(".*[a-z].*");
        System.out.println("Case 3 - Password contains lowercase letter: " + (hasLowercase ? "YES" : "NO"));
        
        // Test Case 4: Password Contains Special Character
        System.out.println("\n=== TEST CASE 4: PASSWORD SPECIAL CHARACTER VALIDATION ===");
        boolean hasSpecialChar = contractorPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");
        System.out.println("Case 4 - Password contains special character: " + (hasSpecialChar ? "YES" : "NO"));
        
        // Test Case 5: Password Length Validation (8-15 characters)
        System.out.println("\n=== TEST CASE 5: PASSWORD LENGTH VALIDATION ===");
        boolean validLength = contractorPassword.length() >= 8 && contractorPassword.length() <= 15;
        System.out.println("Case 5 - Password length (8-15 characters): " + (validLength ? "YES" : "NO"));
        System.out.println("Password length: " + contractorPassword.length() + " characters");
        
        // Test Case 6: Wrong Password API Test
        System.out.println("\n=== TEST CASE 6: WRONG PASSWORD API TEST ===");
        testWrongPasswordAPI();
        
        // Test Case 7: Empty Password API Test
        System.out.println("\n=== TEST CASE 7: EMPTY PASSWORD API TEST ===");
        testEmptyPasswordAPI();
        
        // Test Case 8: Correct Password API Test
        System.out.println("\n=== TEST CASE 8: CORRECT PASSWORD API TEST ===");
        testCorrectPasswordAPI();
        
        System.out.println("\n========== CONTRACTOR SIGNUP COMPLETED ==========");
    }
    
    private void testWrongPasswordAPI() {
        System.out.println("Testing API with wrong password...");
        
        JSONObject wrongPasswordPayload = new JSONObject();
        wrongPasswordPayload.put("email", contractorEmail);
        wrongPasswordPayload.put("password", "wrongpassword123");
        wrongPasswordPayload.put("first_name", contractorFirstName);
        wrongPasswordPayload.put("last_name", contractorLastName);
        wrongPasswordPayload.put("phone_code", "91");
        wrongPasswordPayload.put("phone_no", contractorPhone);
        wrongPasswordPayload.put("device_type", "web");
        wrongPasswordPayload.put("device_name", "");
        wrongPasswordPayload.put("login_address", "");
        wrongPasswordPayload.put("notification", 1);
        
        Response wrongPasswordResponse = given()
                .contentType("application/json")
                .body(wrongPasswordPayload.toString())
                .post(selectedBaseUrl + "/api/contractor/signup");
        
        System.out.println("Case 6 - Wrong Password API Response: " + wrongPasswordResponse.getBody().asString());
        System.out.println("Case 6 - Wrong Password API Status Code: " + wrongPasswordResponse.getStatusCode());
    }
    
    private void testEmptyPasswordAPI() {
        System.out.println("Testing API with empty password...");
        
        JSONObject emptyPasswordPayload = new JSONObject();
        emptyPasswordPayload.put("email", contractorEmail);
        emptyPasswordPayload.put("password", "");
        emptyPasswordPayload.put("first_name", contractorFirstName);
        emptyPasswordPayload.put("last_name", contractorLastName);
        emptyPasswordPayload.put("phone_code", "91");
        emptyPasswordPayload.put("phone_no", contractorPhone);
        emptyPasswordPayload.put("device_type", "web");
        emptyPasswordPayload.put("device_name", "");
        emptyPasswordPayload.put("login_address", "");
        emptyPasswordPayload.put("notification", 1);
        
        Response emptyPasswordResponse = given()
                .contentType("application/json")
                .body(emptyPasswordPayload.toString())
                .post(selectedBaseUrl + "/api/contractor/signup");
        
        System.out.println("Case 7 - Empty Password API Response: " + emptyPasswordResponse.getBody().asString());
        System.out.println("Case 7 - Empty Password API Status Code: " + emptyPasswordResponse.getStatusCode());
    }
    
    private void testCorrectPasswordAPI() {
        System.out.println("Testing API with correct password...");
        
        JSONObject correctPasswordPayload = new JSONObject();
        correctPasswordPayload.put("email", contractorEmail);
        correctPasswordPayload.put("password", contractorPassword);
        correctPasswordPayload.put("first_name", contractorFirstName);
        correctPasswordPayload.put("last_name", contractorLastName);
        correctPasswordPayload.put("phone_code", "91");
        correctPasswordPayload.put("phone_no", contractorPhone);
        correctPasswordPayload.put("device_type", "web");
        correctPasswordPayload.put("device_name", "");
        correctPasswordPayload.put("login_address", "");
        correctPasswordPayload.put("notification", 1);
        
        System.out.println("Correct Password Payload: " + correctPasswordPayload.toString());
        
        Response correctPasswordResponse = given()
                .contentType("application/json")
                .body(correctPasswordPayload.toString())
                .post(selectedBaseUrl + "/api/contractor/signup");
        
        System.out.println("Case 8 - Correct Password API Response: " + correctPasswordResponse.getBody().asString());
        System.out.println("Case 8 - Correct Password API Status Code: " + correctPasswordResponse.getStatusCode());
        
        if (correctPasswordResponse.getStatusCode() == 200) {
            System.out.println("Case 8 - Contractor signup with correct password: SUCCESS!");
        } else {
            System.out.println("Case 8 - Contractor signup with correct password: FAILED!");
        }
    }
    
    @Test(priority = 2, dependsOnMethods = "contractorSignup")
    public void contractorOTPVerification() {
        System.out.println("\n========== CONTRACTOR OTP VERIFICATION ==========");
        
        // Determine if using UAE or EGP database based on selected URL
        boolean isUAE = selectedBaseUrl.contains("reno-dev.azurewebsites.net");
        
        // Fetch OTP from database
        String otp = WebHelper.fetchOTP(isUAE);
        if (otp.isEmpty()) {
            System.out.println("ERROR: No OTP found in database!");
            return;
        }
        
        // Fetch latest user ID from database
        int userId = WebHelper.fetchLatestUserId(isUAE);
        if (userId == 0) {
            System.out.println("ERROR: No user ID found in database!");
            return;
        }
        
        // Prepare OTP verification payload
        JSONObject otpPayload = new JSONObject();
        otpPayload.put("otp", otp);
        otpPayload.put("user_id", userId);
        
        System.out.println("OTP Verification Payload: " + otpPayload.toString());
        
        // Call OTP verification API
        Response otpResponse = given()
                .contentType("application/json")
                .body(otpPayload.toString())
                .post(selectedBaseUrl + "/api/contractor/verify-otp");
        
        System.out.println("OTP Verification API Response: " + otpResponse.getBody().asString());
        System.out.println("OTP Verification API Status Code: " + otpResponse.getStatusCode());
        
        // Validate response
        if (otpResponse.getStatusCode() == 200) {
            String responseMessage = otpResponse.jsonPath().getString("message");
            if (responseMessage != null && responseMessage.contains("verified")) {
                System.out.println("OTP Verification: SUCCESS!");
                System.out.println("Message: " + responseMessage);
            } else {
                System.out.println("OTP Verification: FAILED - Unexpected response message");
            }
        } else {
            System.out.println("OTP Verification: FAILED - Status Code: " + otpResponse.getStatusCode());
        }
        
        System.out.println("========== CONTRACTOR OTP VERIFICATION COMPLETED ==========");
    }
    

}
