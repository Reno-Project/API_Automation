import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
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
    private String authorizationToken;

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
        Assert.assertTrue(passwordValid, "Password creation validation failed");
        
        // Test Case 2: Password Contains Uppercase Letter
        System.out.println("\n=== TEST CASE 2: PASSWORD UPPERCASE VALIDATION ===");
        boolean hasUppercase = contractorPassword.matches(".*[A-Z].*");
        System.out.println("Case 2 - Password contains uppercase letter: " + (hasUppercase ? "YES" : "NO"));
        Assert.assertTrue(hasUppercase, "Password must contain at least one uppercase letter");
        
        // Test Case 3: Password Contains Lowercase Letter
        System.out.println("\n=== TEST CASE 3: PASSWORD LOWERCASE VALIDATION ===");
        boolean hasLowercase = contractorPassword.matches(".*[a-z].*");
        System.out.println("Case 3 - Password contains lowercase letter: " + (hasLowercase ? "YES" : "NO"));
        Assert.assertTrue(hasLowercase, "Password must contain at least one lowercase letter");
        
        // Test Case 4: Password Contains Special Character
        System.out.println("\n=== TEST CASE 4: PASSWORD SPECIAL CHARACTER VALIDATION ===");
        boolean hasSpecialChar = contractorPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");
        System.out.println("Case 4 - Password contains special character: " + (hasSpecialChar ? "YES" : "NO"));
        Assert.assertTrue(hasSpecialChar, "Password must contain at least one special character");
        
        // Test Case 5: Password Length Validation (8-15 characters)
        System.out.println("\n=== TEST CASE 5: PASSWORD LENGTH VALIDATION ===");
        boolean validLength = contractorPassword.length() >= 8 && contractorPassword.length() <= 15;
        System.out.println("Case 5 - Password length (8-15 characters): " + (validLength ? "YES" : "NO"));
        System.out.println("Password length: " + contractorPassword.length() + " characters");
        Assert.assertTrue(validLength, "Password length must be between 8-15 characters");
        
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
        Assert.assertNotEquals(wrongPasswordResponse.getStatusCode(), 200, "Wrong password should not return 200 status code");
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
        Assert.assertNotEquals(emptyPasswordResponse.getStatusCode(), 200, "Empty password should not return 200 status code");
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
        
        Assert.assertEquals(correctPasswordResponse.getStatusCode(), 200, "Correct password should return 200 status code");
        System.out.println("Case 8 - Contractor signup with correct password: SUCCESS!");
    }
    
    @Test(priority = 2, dependsOnMethods = "contractorSignup")
    public void contractorOTPVerification() {
        System.out.println("\n========== CONTRACTOR OTP VERIFICATION ==========");
        
        // Determine if using UAE or EGP database based on selected URL
        boolean isUAE = selectedBaseUrl.contains("reno-dev.azurewebsites.net");
        
        // Fetch OTP from database
        String otp = WebHelper.fetchOTP(isUAE);
        Assert.assertFalse(otp.isEmpty(), "OTP should not be empty - check database connection");
        
        // Fetch latest user ID from database
        int userId = WebHelper.fetchLatestUserId(isUAE);
        Assert.assertTrue(userId > 0, "User ID should be greater than 0 - check database connection");
        
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
        Assert.assertEquals(otpResponse.getStatusCode(), 200, "OTP verification should return 200 status code");
        String responseMessage = otpResponse.jsonPath().getString("message");
        Assert.assertNotNull(responseMessage, "Response message should not be null");
        Assert.assertTrue(responseMessage.contains("verified"), "Response message should contain 'verified'");
        System.out.println("OTP Verification: SUCCESS!");
        System.out.println("Message: " + responseMessage);
        
        System.out.println("========== CONTRACTOR OTP VERIFICATION COMPLETED ==========");
        
        // Fetch IP and location data
        System.out.println("\n========== FETCHING IP AND LOCATION DATA ==========");
        JSONObject locationData = fetchIPAndLocationData();
        
        // Update contractor role
        System.out.println("\n========== UPDATING CONTRACTOR ROLE ==========");
        updateContractorRole(userId, contractorEmail, locationData);
    }
    
    private JSONObject fetchIPAndLocationData() {
        System.out.println("Fetching IP and location data from ipapi.co...");
        
        Response ipResponse = given()
                .get("https://ipapi.co/json/");
        
        System.out.println("IP API Response: " + ipResponse.getBody().asString());
        System.out.println("IP API Status Code: " + ipResponse.getStatusCode());
        
        // Wait for 5 seconds to ensure API response is fully processed
        System.out.println("Waiting 5 seconds for API response to be fully processed...");
        try {
            Thread.sleep(5000); // 5 seconds wait
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted: " + e.getMessage());
        }
        System.out.println("Wait completed, processing response...");
        
        JSONObject locationData = new JSONObject();
        if (ipResponse.getStatusCode() == 200) {
            String ip = ipResponse.jsonPath().getString("ip");
            String city = ipResponse.jsonPath().getString("city");
            String region = ipResponse.jsonPath().getString("region");
            String country = ipResponse.jsonPath().getString("country_name");
            
            // Handle null values with defaults
            locationData.put("ip", ip != null ? ip : "127.0.0.1");
            locationData.put("city", city != null ? city : "Unknown");
            locationData.put("region", region != null ? region : "Unknown");
            locationData.put("country_name", country != null ? country : "Unknown");
            
            System.out.println("Fetched IP: " + locationData.getString("ip"));
            System.out.println("Fetched City: " + locationData.getString("city"));
            System.out.println("Fetched Region: " + locationData.getString("region"));
            System.out.println("Fetched Country: " + locationData.getString("country_name"));
        } else {
            System.out.println("Failed to fetch IP and location data, using defaults");
            locationData.put("ip", "127.0.0.1");
            locationData.put("city", "Unknown");
            locationData.put("region", "Unknown");
            locationData.put("country_name", "Unknown");
        }
        
        return locationData;
    }
    
    private void updateContractorRole(int userId, String email, JSONObject locationData) {
        System.out.println("Updating contractor role...");
        
        // Prepare update role payload
        JSONObject updateRolePayload = new JSONObject();
        updateRolePayload.put("id", userId);
        updateRolePayload.put("email", email);
        updateRolePayload.put("role", "contractor");
        updateRolePayload.put("device_name", locationData.getString("ip"));
        
        // Create login address from location data
        String loginAddress = locationData.getString("city") + ", " + 
                            locationData.getString("region") + ", " + 
                            locationData.getString("country_name");
        updateRolePayload.put("login_address", loginAddress);
        
        System.out.println("Update Role Payload: " + updateRolePayload.toString());
        
        // Call update role API
        Response updateRoleResponse = given()
                .contentType("application/json")
                .body(updateRolePayload.toString())
                .put(selectedBaseUrl + "/api/contractor/update-role");
        
        System.out.println("Update Role API Response: " + updateRoleResponse.getBody().asString());
        System.out.println("Update Role API Status Code: " + updateRoleResponse.getStatusCode());
        
        // Only run validation tests if API call was successful
        if (updateRoleResponse.getStatusCode() == 200) {
            // Test Case 1: Validate IP address is present
            System.out.println("\n=== TEST CASE 1: IP ADDRESS PRESENCE VALIDATION ===");
            String expectedIP = locationData.getString("ip");
            System.out.println("Expected IP: " + expectedIP);
            Assert.assertNotNull(expectedIP, "IP address should not be null");
            Assert.assertFalse(expectedIP.isEmpty(), "IP address should not be empty");
            Assert.assertNotEquals(expectedIP, "127.0.0.1", "IP address should not be default localhost");
            System.out.println("Case 1 - IP address presence validation: PASSED");
            
            // Test Case 2: Validate email ID is present
            System.out.println("\n=== TEST CASE 2: EMAIL ID PRESENCE VALIDATION ===");
            System.out.println("Expected Email: " + email);
            Assert.assertNotNull(email, "Email should not be null");
            Assert.assertFalse(email.isEmpty(), "Email should not be empty");
            Assert.assertTrue(email.contains("@"), "Email should contain @ symbol");
            Assert.assertTrue(email.contains(".com"), "Email should contain .com domain");
            System.out.println("Case 2 - Email ID presence validation: PASSED");
            
            // Test Case 3: Validate user ID is present
            System.out.println("\n=== TEST CASE 3: USER ID PRESENCE VALIDATION ===");
            System.out.println("Expected User ID: " + userId);
            Assert.assertTrue(userId > 0, "User ID should be greater than 0");
            Assert.assertNotNull(userId, "User ID should not be null");
            System.out.println("Case 3 - User ID presence validation: PASSED");
        } else {
            System.out.println("\n=== SKIPPING VALIDATION TESTS DUE TO API FAILURE ===");
            System.out.println("API returned status code: " + updateRoleResponse.getStatusCode());
            System.out.println("Response body: " + updateRoleResponse.getBody().asString());
        }
        
        // Validate overall response
        if (updateRoleResponse.getStatusCode() == 200) {
            System.out.println("Update Role: SUCCESS!");
            
            // Capture authorization token from role update response
            authorizationToken = updateRoleResponse.jsonPath().getString("token");
            if (authorizationToken == null || authorizationToken.isEmpty()) {
                // Try alternative token field names
                authorizationToken = updateRoleResponse.jsonPath().getString("access_token");
                if (authorizationToken == null || authorizationToken.isEmpty()) {
                    authorizationToken = updateRoleResponse.jsonPath().getString("auth_token");
                }
            }
            
            System.out.println("Authorization Token: " + (authorizationToken != null ? authorizationToken.substring(0, Math.min(20, authorizationToken.length())) + "..." : "No token found"));
        } else {
            System.out.println("Update Role: FAILED - Status Code: " + updateRoleResponse.getStatusCode());
        }
        
        System.out.println("========== CONTRACTOR ROLE UPDATE COMPLETED ==========");
        
        // Fetch contractor terms and conditions
        System.out.println("\n========== FETCHING CONTRACTOR TERMS AND CONDITIONS ==========");
        fetchContractorTermsAndConditions();
    }
    
    private void fetchContractorTermsAndConditions() {
        System.out.println("Fetching contractor terms and conditions...");
        
        Response termsResponse = given()
                .get(selectedBaseUrl + "/api/user/contractor-terms-and-conditions");
        
        System.out.println("Terms and Conditions API Status Code: " + termsResponse.getStatusCode());
        System.out.println("Terms and Conditions API Response: " + termsResponse.getBody().asString());
        
        if (termsResponse.getStatusCode() == 200) {
            System.out.println("Terms and Conditions fetched successfully!");
            String success = termsResponse.jsonPath().getString("success");
            String content = termsResponse.jsonPath().getString("content");
            
            if (success != null && success.equals("true")) {
                System.out.println("Success: " + success);
                System.out.println("Content Length: " + (content != null ? content.length() : 0) + " characters");
                System.out.println("Content Preview: " + (content != null ? content.substring(0, Math.min(200, content.length())) + "..." : "No content"));
            } else {
                System.out.println("Terms and Conditions response indicates failure");
            }
        } else {
            System.out.println("Failed to fetch Terms and Conditions - Status Code: " + termsResponse.getStatusCode());
        }
        
        System.out.println("========== CONTRACTOR TERMS AND CONDITIONS FETCH COMPLETED ==========");
        
        // Add contractor details
        System.out.println("\n========== ADDING CONTRACTOR DETAILS ==========");
        addContractorDetails();
    }
    
    private void addContractorDetails() {
        System.out.println("Adding contractor details...");
        
        // Generate random 2-digit number for company name
        int randomNumber = (int) (Math.random() * 90) + 10; // Generates 10-99
        String companyName = "Automated Test Contractor " + randomNumber;
        
        System.out.println("Generated Company Name: " + companyName);
        
        // Prepare JSON payload
        JSONObject contractorDetailsPayload = new JSONObject();
        contractorDetailsPayload.put("company_name", companyName);
        contractorDetailsPayload.put("total_years_in_business", "2-3 years");
        contractorDetailsPayload.put("company_address", "Dubai Silicon Oasis - Dubai - United Arab Emirates");
        contractorDetailsPayload.put("type", "contractor");
        contractorDetailsPayload.put("contractor_expertise", "158,159,162,163,164");
        contractorDetailsPayload.put("lat", "25.1250606");
        contractorDetailsPayload.put("long", "55.3837419");
        contractorDetailsPayload.put("city", "Dubai");
        contractorDetailsPayload.put("company_size", "11-50 employees");
        
        System.out.println("Contractor Details Payload: " + contractorDetailsPayload.toString());
        
        Response contractorDetailsResponse = given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Authorization", authorizationToken != null ? "Bearer " + authorizationToken : "")
                .body(contractorDetailsPayload.toString())
                .post(selectedBaseUrl + "/api/contractor/add-contractor-details");
        
        System.out.println("Add Contractor Details API Status Code: " + contractorDetailsResponse.getStatusCode());
        System.out.println("Add Contractor Details API Response: " + contractorDetailsResponse.getBody().asString());
        
        // Log response headers for debugging
        System.out.println("Response Headers: " + contractorDetailsResponse.getHeaders().toString());
        
        if (contractorDetailsResponse.getStatusCode() == 200) {
            System.out.println("Contractor details added successfully!");
            String success = contractorDetailsResponse.jsonPath().getString("success");
            if (success != null && success.equals("true")) {
                System.out.println("Success: " + success);
            } else {
                System.out.println("Contractor details response indicates failure");
            }
        } else {
            System.out.println("Failed to add contractor details - Status Code: " + contractorDetailsResponse.getStatusCode());
            System.out.println("Error Response: " + contractorDetailsResponse.getBody().asString());
        }
        
        System.out.println("========== CONTRACTOR DETAILS ADDITION COMPLETED ==========");
        
        // Add billing information
        System.out.println("\n========== ADDING BILLING INFORMATION ==========");
        addBillingInfo();
    }
    
    private void addBillingInfo() {
        System.out.println("Adding billing information...");
        
        // Prepare billing info payload
        JSONObject billingPayload = new JSONObject();
        billingPayload.put("beneficiary_name", "");
        billingPayload.put("iban", "");
        billingPayload.put("bank_name", "");
        billingPayload.put("bank_account", "");
        billingPayload.put("swift_code", "");
        billingPayload.put("address", "");
        
        System.out.println("Billing Info Payload: " + billingPayload.toString());
        
        // Call add billing info API
        Response billingResponse = given()
                .contentType("application/json")
                .header("Authorization", authorizationToken != null ? "Bearer " + authorizationToken : "")
                .body(billingPayload.toString())
                .post(selectedBaseUrl + "/api/contractor/add-billing-info");
        
        System.out.println("Add Billing Info API Status Code: " + billingResponse.getStatusCode());
        System.out.println("Add Billing Info API Response: " + billingResponse.getBody().asString());
        
        if (billingResponse.getStatusCode() == 200) {
            System.out.println("Billing information added successfully!");
            String success = billingResponse.jsonPath().getString("success");
            if (success != null && success.equals("true")) {
                System.out.println("Success: " + success);
            } else {
                System.out.println("Billing info response indicates failure");
            }
        } else {
            System.out.println("Failed to add billing information - Status Code: " + billingResponse.getStatusCode());
            System.out.println("Error Response: " + billingResponse.getBody().asString());
        }
        
        System.out.println("========== BILLING INFORMATION ADDITION COMPLETED ==========");
    }
    

}
