import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class OTPHelper {
    public static String fetchLatestOTPFromDB() {
        String latestOTP = "";
        String url = "jdbc:sqlserver://reno-test.database.windows.net;encrypt=true;trustServerCertificate=true;databaseName=reno-test";
        String username = "reno-test";
        String password = "WE}nt.#t4=/ESPz6";

        System.out.println("Starting DB connection to fetch latest OTP...");

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("DB connection successful.");
            String query = "SELECT otp FROM otp WHERE user_id = 907 ORDER BY id DESC";

            System.out.println("Running query: " + query);
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                latestOTP = rs.getString("otp");
                System.out.println("OTP fetched from DB: " + latestOTP);
            } else {
                System.out.println("No OTP found in the database table.");
            }

        } catch (SQLException e) {
            System.out.println("Database error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        return latestOTP;
    }
    public static String fetchLatestClientIdProofId() {
        String latestId = "";
        String url = "jdbc:sqlserver://reno-test.database.windows.net;encrypt=true;trustServerCertificate=true;databaseName=reno-test";
        String username = "reno-test";
        String password = "WE}nt.#t4=/ESPz6";

        System.out.println("Starting DB connection to fetch latest Client ID Proof ID...");

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("DB connection successful.");
            String query = "select id from documents order by id desc";

            System.out.println("Running query: " + query);
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                latestId = rs.getString("id");
                System.out.println("Latest client_id_proof ID fetched: " + latestId);
            } else {
                System.out.println("No records found in client_id_proof table.");
            }

        } catch (SQLException e) {
            System.out.println("Database error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        return latestId;
    }
    public static JSONObject fetchContractDetailsFromDB() {
        String url = "jdbc:sqlserver://reno-test.database.windows.net;encrypt=true;trustServerCertificate=true;databaseName=reno-test";
        String username = "reno-test";
        String password = "WE}nt.#t4=/ESPz6";

        JSONObject payload = new JSONObject();
        JSONObject contractParty = new JSONObject();
        JSONObject individual = new JSONObject();

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("Connected to DB for Contract Details.");

            // Step 1: Fetch from contract_details
            String contractDetailsQuery = "SELECT TOP 1 id, contract_num, plan_id, document_signature_source FROM contract_details ORDER BY id DESC";
            ResultSet rs1 = stmt.executeQuery(contractDetailsQuery);
            int contractId = 0, planId = 0;
            String documentSource = "";
            if (rs1.next()) {
                contractId = rs1.getInt("id");
                planId = rs1.getInt("plan_id");
                documentSource = rs1.getString("document_signature_source");

                payload.put("id", contractId);
                payload.put("contractNum", rs1.getString("contract_num"));
                payload.put("planId", planId);
                payload.put("documentSignatureSource", documentSource);
            }

            // Step 2: Fetch from contract_party
            String contractPartyQuery = "SELECT TOP 1 id, contract_id, type_id FROM contract_party WHERE contract_id = " + contractId + " ORDER BY id DESC";
            ResultSet rs2 = stmt.executeQuery(contractPartyQuery);
            int partyId = 0, individualId = 0;
            if (rs2.next()) {
                partyId = rs2.getInt("id");
                individualId = rs2.getInt("type_id");

                contractParty.put("id", partyId);
                contractParty.put("contractId", contractId);
                contractParty.put("type", "INDIVIDUAL");
                contractParty.put("typeId", individualId);
                contractParty.put("hasPoa", false);
                contractParty.put("signedAt", LocalDate.now() + "T00:00:00.000+00:00");
            }

            // Step 3: Fetch from individual
            String individualQuery = "SELECT TOP 1 id, name, address, nationality, eid, passport_num FROM individual WHERE id = " + individualId;
            ResultSet rs3 = stmt.executeQuery(individualQuery);
            if (rs3.next()) {
                individual.put("id", rs3.getInt("id"));
                individual.put("name", rs3.getString("name"));
                individual.put("address", rs3.getString("address"));
                individual.put("nationality", rs3.getString("nationality"));
                individual.put("eid", rs3.getString("eid"));
                individual.put("passportNum", rs3.getString("passport_num"));
                individual.put("active", true);
            }

            contractParty.put("individual", individual);
            payload.put("contractParty", contractParty);
            payload.put("renoSignedAt", LocalDate.now() + "T00:00:00.000+00:00");

        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
            e.printStackTrace();
        }

        return payload;
    }


}
