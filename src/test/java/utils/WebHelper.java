package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

public class WebHelper {
    
    private static int contractorCounter = 0;
    
    // Database connection details
    private static final String UAE_DB_URL = "jdbc:sqlserver://reno-test.database.windows.net;encrypt=true;trustServerCertificate=true;databaseName=reno-test";
    private static final String EGP_DB_URL = "jdbc:sqlserver://reno-test.database.windows.net;encrypt=true;trustServerCertificate=true;databaseName=reno-test-egp";
    private static final String DB_USER = "reno-test";
    private static final String DB_PASS = "WE}nt.#t4=/ESPz6";
    
    /**
     * Generate random test email for contractor onboarding
     * Format: test.contractor.{randomNumber}@gmail.com
     * @return Random email string
     */
    public static String generateRandomContractorEmail() {
        Random random = new Random();
        int randomNumber = random.nextInt(9999) + 1; // Generate number between 1-9999
        return "test.contractor." + randomNumber + "@gmail.com";
    }
    
    /**
     * Generate random phone number for contractor onboarding
     * Format: 0758348{randomNumber}
     * @return Random phone number string
     */
    public static String generateRandomPhoneNumber() {
        Random random = new Random();
        int randomNumber = random.nextInt(9999) + 1000; // Generate 4-digit number
        return "0758348" + randomNumber;
    }
    
    /**
     * Generate first name
     * Format: Test
     * @return First name string
     */
    public static String generateFirstName() {
        return "Test";
    }
    
    /**
     * Generate last name
     * Format: Contractor
     * @return Last name string
     */
    public static String generateLastName() {
        return "Contractor";
    }
    
    /**
     * Generate random password meeting all requirements:
     * - Contains a number
     * - Contains an uppercase letter
     * - Contains a lowercase letter
     * - Contains a special character symbol
     * - Has at least 8 characters to 15 characters
     * @return Random password string
     */
    public static String generateRandomPassword() {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each category
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));
        
        // Fill remaining length (4-11 more characters) with random characters
        String allChars = uppercase + lowercase + numbers + specialChars;
        int remainingLength = 4 + random.nextInt(8); // 4-11 more characters
        
        for (int i = 0; i < remainingLength; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password to randomize positions
        String shuffledPassword = shuffleString(password.toString());
        return shuffledPassword;
    }
    
    /**
     * Shuffle characters in a string
     * @param input String to shuffle
     * @return Shuffled string
     */
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        Random random = new Random();
        
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = random.nextInt(characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        
        return new String(characters);
    }
    
    /**
     * Validate password meets all requirements
     * @param password Password to validate
     * @return true if password meets all requirements
     */
    public static boolean validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 15) {
            return false;
        }
        
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");
        
        return hasUppercase && hasLowercase && hasNumber && hasSpecialChar;
    }
    
    /**
     * Fetch OTP from database (latest OTP)
     * @param isUAE true for UAE database, false for EGP database
     * @return Latest OTP string
     */
    public static String fetchOTP(boolean isUAE) {
        String dbUrl = isUAE ? UAE_DB_URL : EGP_DB_URL;
        String otp = "";
        
        try {
            Connection connection = DriverManager.getConnection(dbUrl, DB_USER, DB_PASS);
            Statement statement = connection.createStatement();
            
            String query = "SELECT otp FROM otp ORDER BY id DESC";
            ResultSet resultSet = statement.executeQuery(query);
            
            if (resultSet.next()) {
                otp = resultSet.getString("otp");
                System.out.println("Fetched OTP from database: " + otp);
            } else {
                System.out.println("No OTP found in database");
            }
            
            resultSet.close();
            statement.close();
            connection.close();
            
        } catch (Exception e) {
            System.out.println("Error fetching OTP: " + e.getMessage());
        }
        
        return otp;
    }
    
    /**
     * Fetch latest user ID from database
     * @param isUAE true for UAE database, false for EGP database
     * @return Latest user ID
     */
    public static int fetchLatestUserId(boolean isUAE) {
        String dbUrl = isUAE ? UAE_DB_URL : EGP_DB_URL;
        int userId = 0;
        
        try {
            Connection connection = DriverManager.getConnection(dbUrl, DB_USER, DB_PASS);
            Statement statement = connection.createStatement();
            
            String query = "SELECT id FROM users ORDER BY id DESC";
            ResultSet resultSet = statement.executeQuery(query);
            
            if (resultSet.next()) {
                userId = resultSet.getInt("id");
                System.out.println("Fetched latest User ID from database: " + userId);
            } else {
                System.out.println("No user found in database");
            }
            
            resultSet.close();
            statement.close();
            connection.close();
            
        } catch (Exception e) {
            System.out.println("Error fetching User ID: " + e.getMessage());
        }
        
        return userId;
    }

}
