
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.AuthHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.put;

public class HomeOwner_Create_Project {
    @Test
    public void testCreateProjectAndAssignContractor() {
        // Get the HomeOwner Token
        String homeOwnerToken = AuthHelper.getHomeOwnerToken();

        // Create the project
        Response projectResponse = given()
                .header("Authorization", "Bearer " + homeOwnerToken)
                .header("X-Account-ID", "")
                .multiPart("name", "API Automation Sarthak")
                .multiPart("project_type", "Kitchen")
                .multiPart("start_date", "February 03, 2025")
                .multiPart("end_date", "February 24, 2025")
                .multiPart("description", "Test To Check Automation Project Flow")
                .multiPart("exp_id", "2")
                .multiPart("form_json", "{\"appliances\":{\"new_Layouts\":[],\"builtin_appliances\":true,\"new_appliances\":false,\"selected_appliances\":[]},\"kitchenDesignSummary\":{\"data\":{\"projectName\":\"API Automation Sarthak\",\"projectDescription\":\"Test To Check Automation Flow\",\"projectLocation\":\"Dubai\",\"projectSubLocationName\":\"Dubai\",\"projectSubLocation\":\"Dubai\"," +
                        "\"projectType\":\"Kitchen\",\"kitchenLayout\":\"peninsula\",\"size\":\"20\",\"kitchenNewLayout\":[],\"appliances\":[],\"budget\":\"standard\",\"startDate\":\"2025-02-03\",\"endDate\":\"2025-02-24\",\"images\":[],\"builtin_appliances\":true,\"new_appliances\":false," +
                        "\"isLand\":false,\"newLayouts\":false,\"newLighting\":true,\"newFloor\":true,\"cabinets\":true,\"counterTops\":true,\"doorsWindow\":false," +
                        "\"cabinetsWrapping\":true,\"counterTopsWraping\":false}},\"budget_value\":\"3,657\"}")
                .multiPart("layout", "peninsula")
                .multiPart("budget", "standard")
                .multiPart("status", "submitted")
                .post("https://reno-dev.azurewebsites.net/api/project/create-project");

        // ✅ Check project creation response
        System.out.println("Project Response: " + projectResponse.getBody().asString());
        Assert.assertEquals(projectResponse.getStatusCode(), 200, "❌ Project creation failed!");

        // Get Admin Token
        String adminToken = AuthHelper.getAdminToken();

        // Fetch project list
        Response projectListResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .get("https://reno-dev.azurewebsites.net/api/admin/project-list");

        // ✅ Check if project list response is valid
        System.out.println("Project List Response: " + projectListResponse.getBody().asString());
        Assert.assertEquals(projectListResponse.getStatusCode(), 200, "❌ Failed to fetch project list!");

        // ✅ Extract proposal_id safely
        if (projectListResponse.jsonPath().getList("data").isEmpty()) {
            Assert.fail("❌ No projects found in project list!");
        }
        String proposalId = projectListResponse.jsonPath().getString("data[0].id");
        System.out.println("✅ Extracted Proposal ID: " + proposalId);

        // Fetch project proposal details
        Response get_project_proposal_response = given()
                .header("Authorization", "Bearer " + adminToken)
                .get("https://reno-dev.azurewebsites.net/api/project/get-projects?proposal_id=" + proposalId);

        // ✅ Check if project proposal response is valid
        System.out.println("Project_Proposal List Response: " + get_project_proposal_response.getBody().asString());
        Assert.assertEquals(get_project_proposal_response.getStatusCode(), 200, "❌ Failed to fetch project proposal!");

        // ✅ Extract project ID safely
        if (get_project_proposal_response.jsonPath().getList("data").isEmpty()) {
            Assert.fail("❌ No projects found in project proposal response!");
        }
        String projectId = get_project_proposal_response.jsonPath().getString("data[0].id");
        System.out.println("✅ Extracted Project ID: " + projectId);

        // ✅ Assign Contractor using extracted IDs
        assignContractor(projectId, proposalId);
        // ✅ Assign proposalId to proposalCreation
        proposalCreation(proposalId);
        // ✅ Assign projectId to sendProposalToAdmin
        sendProposalToAdmin(projectId);
    }

    public void assignContractor(String projectId, String proposalId) {
        // Get the Admin Token
        String adminToken = AuthHelper.getAdminToken();

        // ✅ Check if projectId and proposalId are valid before proceeding
        if (projectId == null || projectId.isEmpty()) {
            Assert.fail("❌ Project ID is missing or invalid!");
        }
        if (proposalId == null || proposalId.isEmpty()) {
            Assert.fail("❌ Proposal ID is missing or invalid!");
        }

        // Assign contractor API call
        Response response = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("{\"project_id\": " + projectId + ", \"contractor_ids\": [61], \"action\": \"assign\"}")
                .post("https://reno-dev.azurewebsites.net/api/admin/proposal/" + proposalId + "/action");

        // ✅ Validate contractor assignment response
        System.out.println("Assign Contractor Response: " + response.getBody().asString());
        Assert.assertEquals(response.getStatusCode(), 200, "❌ Contractor assignment failed!");
    }

    public void proposalCreation(String proposalId) {
        // Get the contractor token
        String contractorToken = AuthHelper.getContractorToken();
        // Hit GET API to fetch project details using proposal_id
        Response getProjectResponse = given()
                .header("Authorization", "Bearer " + contractorToken)
                .get("https://reno-dev.azurewebsites.net/api/project/get-projects?proposal_id=" + proposalId);
        // Print and validate response
        System.out.println("Proposal_Response: " + getProjectResponse.getBody().asString());
        Assert.assertEquals(getProjectResponse.getStatusCode(), 200);
        System.out.println("**********P R O J E C T - C R E A T I O N - S T A R T E D***********");

        // Define number of milestones and budget items in per milestone
        int numberOfMilestones = 5;
        int numberOfBudgetItems = 5;

        List<Integer> milestoneIds = new ArrayList<>(); // To store the Milestone ID's

        LocalDate startDate = LocalDate.now(); // For set the current date for milestone
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Date Format

        for (int i = 1; i <= numberOfMilestones; i++) {
            String milestoneName = "Test Milestone Number " + i;
            String formattedStartDate = startDate.format(formatter);
            String formattedEndDate = startDate.plusDays(10).format(formatter);

            Response milestoneResponse = given()
                    .header("Authorization", "Bearer " + contractorToken)
                    .contentType("application/json")
                    .body("{\"proposalId\": " + proposalId + ", \"milestoneName\": \"" + milestoneName + "\", " +
                            "\"description\": \"" + milestoneName + "\", " +
                            "\"startDate\": \"" + formattedStartDate + "\", " +
                            "\"endDate\": \"" + formattedEndDate + "\", " +
                            "\"tags\": \"Renovation\"}")
                    .post("https://reno-core-api-test.azurewebsites.net/api/v2/project/milestone");

            Assert.assertEquals(milestoneResponse.getStatusCode(), 201, "❌ Milestone creation failed!");

            int milestoneId = milestoneResponse.jsonPath().getInt("milestoneId");
            milestoneIds.add(milestoneId);

        }
        System.out.println("*********** MILESTONES CREATED SUCCESSFULLY *************");
        // For creation budget items for each milestone
        for (int milestoneId : milestoneIds) {
            for (int j = 1; j <= numberOfBudgetItems; j++) {
                String budgetName = "Test Budget Number " + j;

                int materialUnitPrice = ThreadLocalRandom.current().nextInt(50, 200);
                int quantity = ThreadLocalRandom.current().nextInt(1, 10);
                int manpowerRate = ThreadLocalRandom.current().nextInt(300, 700);
                int days = ThreadLocalRandom.current().nextInt(5, 30);

                Response budgetResponse = given()
                        .header("Authorization", "Bearer " + contractorToken)
                        .contentType("application/json")
                        .body("{\"name\": \"" + budgetName + "\", " +
                                "\"milestoneId\": " + milestoneId + ", " +
                                "\"materialType\": \"Wood\", " +
                                "\"materialUnit\": \"item\", " +
                                "\"materialUnitPrice\": " + materialUnitPrice + ", " +
                                "\"quantity\": " + quantity + ", " +
                                "\"specification\": \"" + budgetName + "\", " +
                                "\"manpowerRate\": " + manpowerRate + ", " +
                                "\"days\": " + days + "}")
                        .post("https://reno-core-api-test.azurewebsites.net/api/v2/project/budget-item");
                Assert.assertEquals(budgetResponse.getStatusCode(), 201, "❌ Budget creation failed!");
            }
        }
        System.out.println("*********** Budget Item Successfully Created**********");
        Response get_Project_Response_With_Milestone_Budget = given()
                .header("Authorization", "Bearer " + contractorToken)
                .get("https://reno-dev.azurewebsites.net/api/project/get-projects?proposal_id=" + proposalId);
        // Print and validate response
        System.out.println("Proposal_Response: " + get_Project_Response_With_Milestone_Budget.getBody().asString());
        Assert.assertEquals(get_Project_Response_With_Milestone_Budget.getStatusCode(), 200);

        System.out.println("*********************** Creating Payment Group ***********************************");
// Generate dueDate from the last milestone's end date
        String lastMilestoneEndDate = startDate.minusDays(5).format(formatter);  // last milestone ka endDate
        String groupId = UUID.randomUUID().toString();  // Generate a random groupId
        System.out.println("GroupID:--" + groupId);
// Create milestones JSON array
        StringBuilder milestonesJson = new StringBuilder("[");
        for (int i = 0; i < milestoneIds.size(); i++) {
            milestonesJson.append("{\"milestoneId\": ").append(milestoneIds.get(i)).append("}");
            if (i < milestoneIds.size() - 1) {
                milestonesJson.append(", ");
            }
        }
        milestonesJson.append("]");

// Create final request body
        String requestBody = "{ \"milestones\": " + milestonesJson + ", " +
                "\"paymentGroup\": {\"groupId\": \"" + groupId + "\", " +  // Add groupId
                "\"groupName\": \"Payment Group Created With Automation\", " +
                "\"refId\": \"" + proposalId + "\", " +
                "\"refType\": \"proposal\", " +
                "\"type\": \"PAYMENT_GROUP\", " +
                "\"dueDate\": \"" + lastMilestoneEndDate + "\"}}";  // Use last milestone's end date

        System.out.println("Payment Group Request Body: " + requestBody);

        Response paymentGroupResponse = given()
                .header("Authorization", "Bearer " + contractorToken)
                .contentType("application/json")
                .body(requestBody)
                .post("https://reno-core-api-test.azurewebsites.net/api/v2/project/payment-group");
        System.out.println("Full Payment Group Response: \n" + paymentGroupResponse.jsonPath().prettyPrint());
        Assert.assertEquals(paymentGroupResponse.getStatusCode(), 200, "❌ Payment Group creation failed!");
        System.out.println("The Payment Group Created Successfully");

    }

    public void sendProposalToAdmin(String projectId) {
        String url = "https://reno-dev.azurewebsites.net/api/project/update-status/" + projectId;
        String contractorToken = AuthHelper.getContractorToken();
        System.out.println("Final API URL: " + url);

        Response sendProposalResponse = given()
                .header("Authorization", "Bearer " + contractorToken)
                .contentType("application/json")
                .body("{\"status\": \"awaiting-approval\"}")
                .put(url);

        System.out.println("Send Proposal Response: " + sendProposalResponse.getBody().asString());
        Assert.assertEquals(sendProposalResponse.getStatusCode(), 200, "❌ Sending proposal to admin failed!");

        System.out.println("✅ Proposal sent to Admin for Approval!");
     commissionDetails();
    }
    public void commissionDetails() {
        String adminToken = AuthHelper.getAdminToken();

        // Fetch Project Data
        Response response = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("https://reno-dev.azurewebsites.net/api/admin/project-list?total=0&pageSize=20&current=1&status=awaiting-approval")
                .then()
                .extract()
                .response();

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.asString());
            if (jsonResponse.getJSONArray("data").length() > 0) {
                JSONObject firstProject = jsonResponse.getJSONArray("data").getJSONObject(0);

                System.out.println("Full Project JSON: " + firstProject.toString(4));

                // Extract correct project ID
                int projectID = firstProject.has("project_id") ? firstProject.getInt("project_id") : firstProject.getInt("id");
                System.out.println("Extracted Project ID: " + projectID);

                // Extract proposal ID
                int proposalId = firstProject.has("proposal_id") ? firstProject.getInt("proposal_id") : -1;
                System.out.println("Extracted Proposal ID: " + proposalId);

                if (proposalId == -1) {
                    System.out.println("❌ Proposal ID not found, skipping API calls.");
                    return;
                }

                double projectCost = firstProject.getDouble("total_amount");

                System.out.println("Project Cost: " + projectCost);

                // Calculate payout amounts
                double initialDeposit = round(projectCost * 0.10);  // 10%
                double projectCompletion = round(projectCost * 0.05); // 5%
                double warrantyCompletion = round(projectCost * 0.10); // 10%
                double renoCommission = round(projectCost * 0.05); // 5%

                //Create Payout Payload for POST API
                JSONObject payoutPayload = new JSONObject();
                payoutPayload.put("Initial_deposit_needed_by_contractor", initialDeposit);
                payoutPayload.put("initial_deposit", initialDeposit);
                payoutPayload.put("completion_of_the_warranty_period", warrantyCompletion);
                payoutPayload.put("contractor_fees", renoCommission);
                payoutPayload.put("project_completion", projectCompletion);
                payoutPayload.put("warranty_period", warrantyCompletion);
                payoutPayload.put("warranty_period_unit", "MONTHS");

                System.out.println("Payout Payload: " + payoutPayload.toString(4));

                //Fetch Project Files
                Response getFilesResponse = given()
                        .header("Authorization", "Bearer " + adminToken)
                        .get("https://reno-dev.azurewebsites.net/api/project/files/" + projectID + "?type=contractor");

                System.out.println("Project Files Response: " + getFilesResponse.getBody().asString());
                Assert.assertEquals(getFilesResponse.getStatusCode(), 200, "❌ Failed to fetch project files!");

                //POST API for Payouts using proposalId
                Response postResponse = given()
                        .header("Authorization", "Bearer " + adminToken)
                        .header("Content-Type", "application/json")
                        .body(payoutPayload.toString())
                        .when()
                        .post("https://reno-core-api-test.azurewebsites.net/api/v2/contractors/proposals/" + proposalId + "/payouts")
                        .then()
                        .extract()
                        .response();

                System.out.println("POST Response: " + postResponse.getBody().asString());
                if(postResponse.getStatusCode() == 200){
                    System.out.println("The project payouts created successfully");
                }else {
                    Assert.fail("❌ Failed to save payout details!");
                }

                //Prepare dynamic payouts list for PUT API
                JSONArray payoutsArray = new JSONArray();
                payoutsArray.put(new JSONObject().put("type", "PAYMENT_GROUP").put("payoutPercent", 70));
                payoutsArray.put(new JSONObject().put("type", "PROJECT_INITIAL_DEPOSIT").put("payoutPercent", 10));
                payoutsArray.put(new JSONObject().put("type", "PROJECT_COMPLETION").put("payoutPercent", 5));
                payoutsArray.put(new JSONObject().put("type", "PROJECT_WARRANTY_COMPLETION").put("payoutPercent", 10));
                payoutsArray.put(new JSONObject().put("type", "RENO_COMMISSION").put("payoutPercent", 5));

                //Create Payload for PUT API
                JSONObject putPayload = new JSONObject(payoutPayload.toString());
                putPayload.put("payouts", payoutsArray);
                putPayload.put("deposit_for_delay_guarantees", 5);
                putPayload.put("weekly_delay", 1);

                //Call PUT API to update fees using projectID
                Response putResponse = given()
                        .header("Authorization", "Bearer " + adminToken)
                        .header("Content-Type", "application/json")
                        .body(putPayload.toString())
                        .when()
                        .put("https://reno-dev.azurewebsites.net/api/admin/fees/" + projectID)
                        .then()
                        .extract()
                        .response();

                System.out.println("PUT Response: " + putResponse.getBody().asString());
                if (putResponse.getStatusCode() == 200){
                    System.out.println("✅ Request sent to Contractor");
                }else {
                    Assert.fail("❌ API success false — Request not sent to the contractor.");
                }

            }
        }
    }

    private double round(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }



}