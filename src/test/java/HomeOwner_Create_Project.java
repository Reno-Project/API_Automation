import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.AuthHelper;
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
                .multiPart("name", "API Automation Project")
                .multiPart("project_type", "Kitchen")
                .multiPart("start_date", "February 03, 2025")
                .multiPart("end_date", "February 24, 2025")
                .multiPart("description", "Test To Check Automation Project Flow")
                .multiPart("exp_id", "2")
                .multiPart("form_json", "{\"appliances\":{\"new_Layouts\":[],\"builtin_appliances\":true,\"new_appliances\":false,\"selected_appliances\":[]},\"kitchenDesignSummary\":{\"data\":{\"projectName\":\"API Automation Project\",\"projectDescription\":\"Test To Check Automation Flow\",\"projectLocation\":\"Dubai\",\"projectSubLocationName\":\"Dubai\",\"projectSubLocation\":\"Dubai\"," +
                        "\"projectType\":\"Kitchen\",\"kitchenLayout\":\"peninsula\",\"size\":\"20\",\"kitchenNewLayout\":[],\"appliances\":[],\"budget\":\"standard\",\"startDate\":\"2025-02-03\",\"endDate\":\"2025-02-24\",\"images\":[],\"builtin_appliances\":true,\"new_appliances\":false," +
                        "\"isLand\":false,\"newLayouts\":false,\"newLighting\":true,\"newFloor\":true,\"cabinets\":true,\"counterTops\":true,\"doorsWindow\":false," +
                        "\"cabinetsWrapping\":true,\"counterTopsWraping\":false}},\"budget_value\":\"3,657\"}")
                .multiPart("layout", "peninsula")
                .multiPart("budget", "standard")
                .multiPart("status", "submitted")
                .post("https://reno-dev.azurewebsites.net/api/project/create-project");

        // Print the response
        System.out.println("Project Response: " + projectResponse.getBody().asString());
        Assert.assertEquals(projectResponse.getStatusCode(), 200);

        // Now fetch the project list to get the proposal ID
        String adminToken = AuthHelper.getAdminToken();
        Response projectListResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .get("https://reno-dev.azurewebsites.net/api/admin/project-list");

        // Check if project list is fetched successfully
        System.out.println("Project List Response: " + projectListResponse.getBody().asString());
        Assert.assertEquals(projectListResponse.getStatusCode(), 200);

        // Extract proposal_id of the latest project (top of the list)
        String proposalId = projectListResponse.jsonPath().getString("data[0].id");
        System.out.println("Extracted Proposal ID: " + proposalId);

        // Get the project/Proposal response
        Response get_project_proposal_response = given()
                .header("Authorization", "Bearer " + adminToken)
                .get("https://reno-dev.azurewebsites.net/api/project/get-projects?proposal_id=" + proposalId);
        // Validate the response.
        System.out.println("Project_Proposal List Response: " + get_project_proposal_response.getBody().asString());
        Assert.assertEquals(get_project_proposal_response.getStatusCode(), 200);

        // Extract both project_id and proposal_id from the response
        String projectId = get_project_proposal_response.jsonPath().getString("data[0].id");
        System.out.println("Extracted Project ID: " + projectId);

        // Assign Contractor using both proposal_id (in URL) and project_id (in body)
        assignContractor(projectId, proposalId);
    }

    public void assignContractor(String projectId, String proposalId) {
        // Get the Admin Token
        String adminToken = AuthHelper.getAdminToken();

        // Assign contractor API call
        Response response = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("{\"project_id\": " + projectId + ", \"contractor_ids\": [61], \"action\": \"assign\"}")
                .post("https://reno-dev.azurewebsites.net/api/admin/proposal/" + proposalId + "/action");

        // Print the response
        System.out.println("Assign Contractor Response: " + response.getBody().asString());
        Assert.assertEquals(response.getStatusCode(), 200);
    }
}
