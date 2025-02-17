import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.AuthHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

                Response budgetResponse = given()
                        .header("Authorization", "Bearer " + contractorToken)
                        .contentType("application/json")
                        .body("{\"name\": \"" + budgetName + "\", " +
                                "\"milestoneId\": " + milestoneId + ", " +
                                "\"materialType\": \"Wood\", " +
                                "\"materialUnit\": \"item\", " +
                                "\"materialUnitPrice\": \"80\", " +
                                "\"quantity\": \"2\", " +
                                "\"specification\": \"" + budgetName + "\", " +
                                "\"manpowerRate\": 100, " +
                                "\"days\": 2}")
                        .post("https://reno-core-api-test.azurewebsites.net/api/v2/project/budget-item");
                Assert.assertEquals(budgetResponse.getStatusCode(), 201, "❌ Budget creation failed!");
                System.out.println("*********** BUDGET ITEMS CREATED SUCCESSFULLY *************");

            }
        }
    }
}
