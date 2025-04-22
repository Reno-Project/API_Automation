import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OTPHelper {
    public static String fetchLatestOTPFromDB() {
        String latestOTP = "";
        String url = "jdbc:sqlserver://reno-test.database.windows.net;encrypt=true;trustServerCertificate=true;databaseName=reno-test";
        String username = "reno-test";
        String password = "WE}nt.#t4=/ESPz6";

        System.out.println("🔍 Starting DB connection to fetch latest OTP...");

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("✅ DB connection successful.");
            String query = "SELECT otp FROM otp WHERE user_id = 907 ORDER BY id DESC";

            System.out.println("📥 Running query: " + query);
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                latestOTP = rs.getString("otp");
                System.out.println("✅ OTP fetched from DB: " + latestOTP);
            } else {
                System.out.println("⚠️ No OTP found in the database table.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Database error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        return latestOTP;
    }
    public static String fetchLatestClientIdProofId() {
        String latestId = "";
        String url = "jdbc:sqlserver://reno-test.database.windows.net;encrypt=true;trustServerCertificate=true;databaseName=reno-test";
        String username = "reno-test";
        String password = "WE}nt.#t4=/ESPz6";

        System.out.println("🔍 Starting DB connection to fetch latest Client ID Proof ID...");

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("✅ DB connection successful.");
            String query = "SELECT id FROM client_id_proof ORDER BY id DESC";

            System.out.println("📥 Running query: " + query);
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                latestId = rs.getString("id");
                System.out.println("✅ Latest client_id_proof ID fetched: " + latestId);
            } else {
                System.out.println("⚠️ No records found in client_id_proof table.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Database error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        return latestId;
    }

}
