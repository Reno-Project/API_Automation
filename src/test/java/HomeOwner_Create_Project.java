import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.AuthHelper;
import java.util.UUID;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;

public class HomeOwner_Create_Project {
    @Test
    public void testCreateProjectAndAssignContractor() {
        // Get the HomeOwner Token
        String homeOwnerToken = AuthHelper.getHomeOwnerToken();

        // Create the project
        Response projectResponse = given()
                .header("Authorization", "Bearer " + homeOwnerToken)
                .header("X-Account-ID", "")
                .multiPart("name", "API Automation Sarthak Test")
                .multiPart("project_type", "Kitchen")
                .multiPart("start_date", "February 03, 2025")
                .multiPart("end_date", "February 24, 2025")
                .multiPart("description", "Test To Check Automation Project Flow")
                .multiPart("exp_id", "2")
                .multiPart("form_json", "{\"appliances\":{\"new_Layouts\":[],\"builtin_appliances\":true,\"new_appliances\":false,\"selected_appliances\":[]},\"kitchenDesignSummary\":{\"data\":{\"projectName\":\"API Automation Sarthak Test\",\"projectDescription\":\"Test To Check Automation Flow\",\"projectLocation\":\"Dubai\",\"projectSubLocationName\":\"Dubai\",\"projectSubLocation\":\"Dubai\"," +
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
    public void proposalCreation(String proposalId){
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


        }
}