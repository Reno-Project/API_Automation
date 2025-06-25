
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.AuthHelper;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;

public class HomeOwner_Create_Project {

    String homeOwnerToken;
    String dynamicProjectName;
    String startDate;
    String endDate;
    String adminToken;
    String projectId;
    String proposalId;

    @Test(priority = 1)
    public void createProject() {
        homeOwnerToken = AuthHelper.getHomeOwnerToken();

        long randomNumber = System.currentTimeMillis() % 100000;
        dynamicProjectName = "Project_Number: " + randomNumber;

        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        startDate = today.format(formatter);
        endDate = nextMonth.format(formatter);

        System.out.println("Project Name: " + dynamicProjectName);
        System.out.println("Start Date: " + startDate);
        System.out.println("End Date: " + endDate);

        Response projectResponse = given()
                .header("Authorization", "Bearer " + homeOwnerToken)
                .multiPart("name", dynamicProjectName)
                .multiPart("project_type", "Kitchen")
                .multiPart("start_date", startDate)
                .multiPart("end_date", endDate)
                .multiPart("description", "Test To Check Automation Project Flow")
                .multiPart("exp_id", "2")
                .multiPart("form_json", "{\"appliances\":{\"new_Layouts\":[],\"builtin_appliances\":true,\"new_appliances\":false,\"selected_appliances\":[]},\"kitchenDesignSummary\":{\"data\":{\"projectName\":\"" + dynamicProjectName + "\",\"projectDescription\":\"Test To Check Automation Flow\",\"projectLocation\":\"Dubai\",\"projectSubLocationName\":\"Dubai\",\"projectSubLocation\":\"Dubai\"," +
                        "\"projectType\":\"Kitchen\",\"kitchenLayout\":\"peninsula\",\"size\":\"20\",\"kitchenNewLayout\":[],\"appliances\":[],\"budget\":\"standard\",\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate + "\",\"images\":[],\"builtin_appliances\":true,\"new_appliances\":false," +
                        "\"isLand\":false,\"newLayouts\":false,\"newLighting\":true,\"newFloor\":true,\"cabinets\":true,\"counterTops\":true,\"doorsWindow\":false," +
                        "\"cabinetsWrapping\":true,\"counterTopsWraping\":false}},\"budget_value\":\"121,120\"}")
                .multiPart("layout", "peninsula")
                .multiPart("budget", "standard")
                .multiPart("status", "submitted")
                .post("https://reno-dev.azurewebsites.net/api/project/create-project");

        System.out.println("Project Creation Response: " + projectResponse.getBody().asString());
        Assert.assertEquals(projectResponse.getStatusCode(), 200, "[TC_03] Project creation failed!");
    }

    @Test(priority = 2, dependsOnMethods = "createProject")
    public void validateStartAndEndDate() {
        adminToken = AuthHelper.getAdminToken();

        Response projectListResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .get("https://reno-dev.azurewebsites.net/api/admin/project-list");

        System.out.println("✅ Project List Response: " + projectListResponse.getBody().asString());
        Assert.assertEquals(projectListResponse.getStatusCode(), 200, "Failed to fetch project list!");

        List<Map<String, Object>> projects = projectListResponse.jsonPath().getList("data");
        boolean projectFound = false;
        for (Map<String, Object> project : projects) {
            String projectNameInResponse = (String) project.get("name");

            if (projectNameInResponse != null && projectNameInResponse.equals(dynamicProjectName)) {
                projectFound = true;

                String startDateInResponse = (String) project.get("start_date");
                String endDateInResponse = (String) project.get("end_date");

                Object idObject = project.get("id");
                if (idObject != null) {
                    projectId = String.valueOf(idObject);
                }

                Map<String, Object> proposalData = (Map<String, Object>) project.get("proposal");
                if (proposalData != null) {
                    Object proposalIdObj = proposalData.get("id");
                    if (proposalIdObj != null) {
                        proposalId = String.valueOf(proposalIdObj);
                    }
                }

                System.out.println("✅ Found Project Name: " + projectNameInResponse);
                System.out.println("[TC_01] Expected Start Date: " + startDate + " | Actual Start Date: " + startDateInResponse);
                System.out.println("[TC_02] Expected End Date: " + endDate + " | Actual End Date: " + endDateInResponse);

                Assert.assertEquals(startDateInResponse, startDate, "[TC_01] Start date mismatch!");
                Assert.assertEquals(endDateInResponse, endDate, "[TC_02] End date mismatch!");
                break;
            }
        }


        if (!projectFound) {
            Assert.fail("Project with name " + dynamicProjectName + " not found in project list!");
        }
    }

    @Test(priority = 3, dependsOnMethods = "validateStartAndEndDate")
    public void fetchAndValidateProjectData() {
        if (projectId == null || proposalId == null) {
            Assert.fail("ProjectId or ProposalId not available to continue the flow!");
        }

        Response getProjectProposalResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .get("https://reno-dev.azurewebsites.net/api/project/get-projects?proposal_id=" + proposalId);

        System.out.println("Project Proposal List Response: " + getProjectProposalResponse.getBody().asString());
        Assert.assertEquals(getProjectProposalResponse.getStatusCode(), 200, "Failed to fetch project proposal!");

        if (getProjectProposalResponse.jsonPath().getList("data").isEmpty()) {
            Assert.fail("No projects found in project proposal response!");
        }

        String projectIdFromProposal = getProjectProposalResponse.jsonPath().getString("data[0].id");
        System.out.println("Extracted Project ID from Proposal API: " + projectIdFromProposal);

        //Call the required methods
        assignContractor(projectIdFromProposal, proposalId);
        proposalCreation(projectIdFromProposal,proposalId);
        sendProposalToAdmin(projectIdFromProposal);
    }

    public void assignContractor(String projectIdFromProposal, String proposalId) {
        //Get Admin Token
        String adminToken = AuthHelper.getAdminToken();

        //Validate IDs
        if (projectId == null || projectId.isEmpty()) {
            Assert.fail("Project ID is missing or invalid!");
        }
        if (proposalId == null || proposalId.isEmpty()) {
            Assert.fail("Proposal ID is missing or invalid!");
        }

        //Log IDs before API call
        System.out.println("Assigning contractor for:");
        System.out.println("projectId: " + projectIdFromProposal);
        System.out.println("proposalId: " + proposalId);

        //Prepare body
        String requestBody = "{ \"project_id\": " + projectIdFromProposal + ", \"contractor_ids\": [61], \"action\": \"assign\" }";

        //Send POST request
        Response response = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body(requestBody)
                .post("https://reno-dev.azurewebsites.net/api/admin/proposal/" + proposalId + "/action");

        //Print response
        System.out.println("Assign Contractor Response: " + response.getBody().asPrettyString());
        System.out.println("Status Code: " + response.getStatusCode());

        //Check success manually from response body also
        if (response.getStatusCode() != 200 || !response.getBody().asString().contains("\"success\":true")) {
            Assert.fail("Contractor assignment failed! Either project/proposal is invalid or already assigned.");
        }

        System.out.println("Contractor assigned successfully to project " + projectIdFromProposal);
    }


    public void proposalCreation(String projectIdFromproposalId,String proposalId) {
        // Get the contractor token
        String contractorToken = AuthHelper.getContractorToken();
        // Hit GET API to fetch project details using proposal_id
        Response getProjectResponse = given()
                .header("Authorization", "Bearer " + contractorToken)
                .get("https://reno-dev.azurewebsites.net/api/project/get-projects?proposal_id=" + proposalId);
        // Print and validate response
        System.out.println("Proposal_Response: " + getProjectResponse.getBody().asString());
        Assert.assertEquals(getProjectResponse.getStatusCode(), 200);

        // PATCH request payload
        String payload = "{\n" +
                "  \"project_id\": " + projectIdFromproposalId + ",\n" +
                "  \"data\": {\n" +
                "    \"client_type\": \"Homeowner\",\n" +
                "    \"location\": \"Dubai Mall - Dubai - United Arab Emirates\",\n" +
                "    \"scope\": \"Renovation\",\n" +
                "    \"unit_type\": \"Villa\",\n" +
                "    \"unit\": \"10\",\n" +
                "    \"unit_size\": \"500\"\n" +
                "  }\n" +
                "}";
        // Hit PATCH API
        Response patchResponse = given()
                .header("Authorization", "Bearer " + contractorToken)
                .contentType("application/json")
                .body(payload)
                .patch("https://reno-dev.azurewebsites.net/api/admin/update-project-details");

        // Print response
        System.out.println("Update Project PATCH Response: " + patchResponse.getBody().asString());
        Assert.assertEquals(patchResponse.getStatusCode(), 200, "Failed to update project details!");

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

    public void sendProposalToAdmin(String projectIdFromProposal) {
        String url = "https://reno-dev.azurewebsites.net/api/project/update-status/" + projectIdFromProposal;
        String contractorToken = AuthHelper.getContractorToken();
        System.out.println("Final API URL: " + url);

        Response sendProposalResponse = given()
                .header("Authorization", "Bearer " + contractorToken)
                .contentType("application/json")
                .body("{\"status\": \"awaiting-approval\"}")
                .put(url);

        System.out.println("Send Proposal Response: " + sendProposalResponse.getBody().asString());
        Assert.assertEquals(sendProposalResponse.getStatusCode(), 200, "❌ Sending proposal to admin failed!");

        System.out.println("Proposal sent to Admin to add the Commission Details!");
        commissionDetails();
    }

    public void commissionDetails() {
        String adminToken = AuthHelper.getAdminToken();

        // Fetch Project Data
        Response response = given()
                .header("Authorization", "Bearer " + adminToken)
                .get("https://reno-dev.azurewebsites.net/api/admin/project-list?total=0&pageSize=20&current=1&status=awaiting-approval")
                .then()
                .extract()
                .response();

        int proposalId = 0;
        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.asString());
            if (jsonResponse.getJSONArray("data").length() > 0) {
                JSONObject firstProject = jsonResponse.getJSONArray("data").getJSONObject(0);

                System.out.println("Full Project JSON: " + firstProject.toString(4));

                int projectID = firstProject.has("project_id") ? firstProject.getInt("project_id") : firstProject.getInt("id");
                System.out.println("Extracted Project ID: " + projectID);

                proposalId = firstProject.has("proposal_id") ? firstProject.getInt("id") : -1;
                System.out.println("Extracted Proposal ID: " + proposalId);

                if (proposalId == -1) {
                    System.out.println("❌ Proposal ID not found, skipping API calls.");
                    return;
                }

                double projectCost = firstProject.getDouble("total_amount");
                System.out.println("Project Cost: " + projectCost);

                double initialDepositPercent = 10;
                double projectCompletionPercent = 5;
                double warrantyCompletionPercent = 5;
                double renoCommissionPercent = 5;
                double weeklyDelayPercent = 1;
                double depositForDelayGuaranteesPercent = 5;

                double fixedPercentTotal = initialDepositPercent + projectCompletionPercent + warrantyCompletionPercent
                        + renoCommissionPercent + weeklyDelayPercent + depositForDelayGuaranteesPercent;

                double contractorFeePercent = 100 - fixedPercentTotal;

                System.out.println("Final Split:");
                System.out.println("Initial Deposit: " + initialDepositPercent + "%");
                System.out.println("Project Completion: " + projectCompletionPercent + "%");
                System.out.println("Warranty Completion: " + warrantyCompletionPercent + "%");
                System.out.println("Reno Commission: " + renoCommissionPercent + "%");
                System.out.println("Weekly Delay: " + weeklyDelayPercent + "%");
                System.out.println("Deposit For Delay Guarantee: " + depositForDelayGuaranteesPercent + "%");
                System.out.println("Contractor Fees: " + contractorFeePercent + "%");

                JSONObject payoutPayload = new JSONObject();
                payoutPayload.put("Initial_deposit_needed_by_contractor", initialDepositPercent);
                payoutPayload.put("initial_deposit", initialDepositPercent);
                payoutPayload.put("completion_of_the_warranty_period", warrantyCompletionPercent);
                payoutPayload.put("contractor_fees", contractorFeePercent);
                payoutPayload.put("project_completion", projectCompletionPercent);
                payoutPayload.put("warranty_period", warrantyCompletionPercent);
                payoutPayload.put("warranty_period_unit", "MONTHS");
                payoutPayload.put("deposit_for_delay_guarantees", depositForDelayGuaranteesPercent);
                payoutPayload.put("weekly_delay", weeklyDelayPercent);
                payoutPayload.put("reno_fee", renoCommissionPercent);

                System.out.println("Final Payload:\n" + payoutPayload.toString(4));

                Response getFilesResponse = given()
                        .header("Authorization", "Bearer " + adminToken)
                        .get("https://reno-dev.azurewebsites.net/api/project/files/" + projectID + "?type=contractor");

                System.out.println("Project Files Response: " + getFilesResponse.getBody().asString());
                Assert.assertEquals(getFilesResponse.getStatusCode(), 200, "❌ Failed to fetch project files!");

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

                if (postResponse.getStatusCode() == 200) {
                    System.out.println("✅ The project payouts created successfully");
                } else {
                    Assert.fail("❌ Failed to save payout details!");
                }

                JSONArray payoutsArray = new JSONArray();
                payoutsArray.put(new JSONObject().put("type", "PAYMENT_GROUP").put("payoutPercent", 70));
                payoutsArray.put(new JSONObject().put("type", "PROJECT_INITIAL_DEPOSIT").put("payoutPercent", 10));
                payoutsArray.put(new JSONObject().put("type", "PROJECT_COMPLETION").put("payoutPercent", 5));
                payoutsArray.put(new JSONObject().put("type", "PROJECT_WARRANTY_COMPLETION").put("payoutPercent", 10));
                payoutsArray.put(new JSONObject().put("type", "RENO_COMMISSION").put("payoutPercent", 5));

                JSONObject putPayload = new JSONObject(payoutPayload.toString());
                putPayload.put("payouts", payoutsArray);
                putPayload.put("deposit_for_delay_guarantees", 5);
                putPayload.put("weekly_delay", 1);

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
                if (putResponse.getStatusCode() == 200) {
                    System.out.println("✅ Request sent to Contractor");
                } else {
                    Assert.fail("❌API success false — Request not sent to the contractor.");
                }
            }
        }
        Approve_commissionDetails(String.valueOf(proposalId));
    }

    private double round(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }


    public void Approve_commissionDetails(String proposalId) {
        String contractorToken = AuthHelper.getContractorToken();

        //GET project details using proposalId
        Response getProjectResponse = given()
                .header("Authorization", "Bearer " + contractorToken)
                .when()
                .get("https://reno-dev.azurewebsites.net/api/project/get-projects?proposal_id=" + proposalId)
                .then()
                .extract()
                .response();

        System.out.println("For_Approve_CommissionDetails ------------> " + proposalId);
        System.out.println(getProjectResponse.getBody().asString());

        if (getProjectResponse.getStatusCode() == 200) {
            System.out.println("✅ Approve Commission Details Fetched Successfully");

            //Extract project_id from response
            JSONObject jsonResponse = new JSONObject(getProjectResponse.getBody().asString());
            if (jsonResponse.has("data") && jsonResponse.getJSONArray("data").length() > 0) {
                int projectId = jsonResponse.getJSONArray("data").getJSONObject(0).getInt("id");
                System.out.println("Extracted Project ID for Approval: " + projectId);

                //Prepare payload
                JSONObject payload = new JSONObject();
                payload.put("project_id", projectId);
                payload.put("status", "approve");

                //Call PUT API to approve
                Response approveResponse = given()
                        .header("Authorization", "Bearer " + contractorToken)
                        .header("Content-Type", "application/json")
                        .body(payload.toString())
                        .when()
                        .put("https://reno-dev.azurewebsites.net/api/project/status")
                        .then()
                        .extract()
                        .response();

                //Print and validate
                System.out.println("PUT Approval Response: " + approveResponse.getBody().asString());

                Assert.assertEquals(approveResponse.getStatusCode(), 200, "❌ Failed to approve project!");
                Assert.assertTrue(approveResponse.asString().contains("success"), "❌ Approval not successful!");
                System.out.println("✅ Project Approved Successfully!");

            } else {
                Assert.fail("❌ No project found in GET response for proposalId: " + proposalId);
            }

        } else {
            Assert.fail("❌ Failed to fetch approve commission details");
        }
        addHomeOwnerPrice();
    }
    public void addHomeOwnerPrice() {
        final String adminBaseUrl = "https://reno-dev.azurewebsites.net";
        final String coreBaseUrl  = "https://reno-core-api-test.azurewebsites.net";
        final String adminToken   = AuthHelper.getAdminToken();

        Response listResp = given()
                .header("Authorization", "Bearer " + adminToken)
                .get(adminBaseUrl + "/api/admin/project-list?total=0&pageSize=20&current=1&status=awaiting-project-pricing")
                .then().extract().response();

        if (listResp.statusCode() != 200) {
            System.out.println("❌ Failed to fetch project list → " + listResp.asString());
            return;
        }

        JSONArray dataArr = new JSONObject(listResp.asString()).getJSONArray("data");
        if (dataArr.isEmpty()) {
            System.out.println("❌ No projects found with status awaiting-project-pricing.");
            return;
        }

        JSONObject firstProject = dataArr.getJSONObject(0);
        final int    proposalId  = firstProject.getInt("id");
        final int    projectId   = firstProject.getInt("project_id");
        final double totalAmount = Double.parseDouble(firstProject.get("total_amount").toString());
        final double proposalPrice = round(totalAmount * 1.10);   // +10 %
        final String proposalEndDt = "June 23, 2025";            // dummy date sample

        System.out.printf("👉 Using proposalId=%d | projectId=%d | price=%.2f%n", proposalId, projectId, proposalPrice);

        JSONArray paymentPlanConfig = new JSONArray()
                .put(new JSONObject()
                        .put("planType", "MONTHS_3")
                        .put("months", 3)
                        .put("markup", 5)
                        .put("downpayment", 30)
                        .put("moveInPayment", 10)
                        .put("projectPaymentType", "RNPL"))
                .put(new JSONObject()
                        .put("planType", "MONTHS_6")
                        .put("months", 6)
                        .put("markup", 10)
                        .put("downpayment", 25)
                        .put("moveInPayment", 10)
                        .put("projectPaymentType", "RNPL"))
                .put(new JSONObject()
                        .put("planType", "MONTHS_12")
                        .put("months", 12)
                        .put("markup", 15)
                        .put("downpayment", 10)
                        .put("moveInPayment", 10)
                        .put("projectPaymentType", "RNPL"))
                .put(new JSONObject()                          // CUSTOM / ESCROW
                        .put("planType", "CUSTOM")
                        .put("months", 0)
                        .put("markup", 0)
                        .put("downpayment", 50)
                        .put("moveInPayment", 20)
                        .put("projectPaymentType", "ESCROW"));


        JSONObject viewPayload = new JSONObject()
                .put("proposalId", proposalId)
                .put("proposalPrice", String.valueOf(proposalPrice))
                .put("proposalEndDt", proposalEndDt)
                .put("paymentPlanConfig", paymentPlanConfig);

        Response viewResp = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body(viewPayload.toString())
                .post(coreBaseUrl + "/api/v2/payment-plan/proposal/" + proposalId + "/view");

        System.out.println("/view response → " + viewResp.asString());

        JSONObject putPayload = new JSONObject()
                .put("proposal_id", proposalId)
                .put("amount", String.valueOf(proposalPrice))
                .put("payment_type", "RNPL")   // business still tags RNPL as master type
                .put("show_price", false)
                .put("payment_plan", paymentPlanConfig);

        Response putResp = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body(putPayload.toString())
                .put(adminBaseUrl + "/api/project/price");

        if (putResp.statusCode() == 200) {
            System.out.println("✅ Homeowner price saved successfully!");
        } else {
            System.out.println("❌ Failed to save price → " + putResp.asString());
        }

        fetchProjectDetailsByProposalId(String.valueOf(proposalId), projectId);
    }
    public void fetchProjectDetailsByProposalId(String proposalId, int projectId) {
        String adminToken = AuthHelper.getAdminToken();

        //Project Details
        Response projectDetailsResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("https://reno-dev.azurewebsites.net/api/project/get-project-details?project_id=" + projectId)
                .then()
                .extract()
                .response();

        System.out.println("GET /project/get-project-details?project_id=" + projectId);
        System.out.println("Response Code: " + projectDetailsResponse.getStatusCode());
        System.out.println("Response Body: " + projectDetailsResponse.getBody().asString());

        if (projectDetailsResponse.getStatusCode() != 200) {
            Assert.fail("❌ Failed to fetch project details for project ID: " + projectId);
        }

        //KYB Documents
        Response kybDocumentsResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("https://reno-dev.azurewebsites.net/api/user/project/" + projectId + "/kyb-documents")
                .then()
                .extract()
                .response();

        System.out.println("GET /user/project/" + projectId + "/kyb-documents");
        System.out.println("Response Code: " + kybDocumentsResponse.getStatusCode());
        System.out.println("Response Body: " + kybDocumentsResponse.getBody().asPrettyString());

        if (kybDocumentsResponse.getStatusCode() != 200) {
            Assert.fail("❌ Failed to fetch KYB documents for project ID: " + projectId);
        }

        //Contract Details
        Response contractDetailsResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("https://reno-core-api-test.azurewebsites.net/api/v2/contracts/details/project/" + projectId)
                .then()
                .extract()
                .response();

        System.out.println("GET /contracts/details/project/" + projectId);
        System.out.println("Response Code: " + contractDetailsResponse.getStatusCode());
        System.out.println("Response Body: " + contractDetailsResponse.getBody().asPrettyString());

        // Just log if contract exists or not, don’t fail
        if (contractDetailsResponse.getStatusCode() == 400) {
            System.out.println("🟡 Contract does not exist yet. Proceeding to create contract...");
        } else if (contractDetailsResponse.getStatusCode() == 200) {
            System.out.println("✅ Contract already exists for projectId: " + projectId);
            // You can choose to exit early or continue based on your use-case
        } else {
            System.out.println("❌ Unexpected response while checking contract details. Status: " +
                    contractDetailsResponse.getStatusCode());
            // Optional: throw new RuntimeException or handle accordingly
        }


        //Payment Lines using proposalId directly
        Response paymentLinesResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("https://reno-core-api-test.azurewebsites.net/api/v2/paymentlines/proposal/" + proposalId)
                .then()
                .extract()
                .response();

        System.out.println("GET /paymentlines/proposal/" + proposalId);
        System.out.println("Response Code: " + paymentLinesResponse.getStatusCode());
        System.out.println("Response Body: " + paymentLinesResponse.getBody().asPrettyString());

        if (paymentLinesResponse.getStatusCode() != 200) {
            Assert.fail("❌ Failed to fetch payment lines for proposal ID: " + proposalId);
        }

        System.out.println("✅ All 4 API calls completed successfully.");
        //POST Contract Details
        JSONObject payload = new JSONObject();
        payload.put("projectId", projectId);
        payload.put("proposalId", Integer.parseInt(proposalId)); // convert String to int

        JSONObject contractParty = new JSONObject();
        contractParty.put("type", "INDIVIDUAL");
        contractParty.put("hasPoa", false);
        contractParty.put("signedAt", JSONObject.NULL);

        JSONObject individual = new JSONObject();
        individual.put("name", "Sarthak");
        individual.put("address", "Test dubai");
        individual.put("nationality", "Dubai");
        individual.put("eid", "SARTH061799");
        individual.put("passportNum", "SARTH061799");

        contractParty.put("individual", individual);
        payload.put("contractParty", contractParty);

        Response postContractResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .header("Content-Type", "application/json")
                .body(payload.toString())
                .when()
                .post("https://reno-core-api-test.azurewebsites.net/api/v2/contracts/details")
                .then()
                .extract()
                .response();

        System.out.println("📩 POST /contracts/details");
        System.out.println("Response Code: " + postContractResponse.getStatusCode());
        System.out.println("Response Body: " + postContractResponse.getBody().asPrettyString());

        if (postContractResponse.getStatusCode() != 200 && postContractResponse.getStatusCode() != 201) {
            Assert.fail("❌ Failed to submit contract details.");
        }

        // 6️⃣ PUT Update Project Status
        JSONObject statusPayload = new JSONObject();
        statusPayload.put("status", "awaiting-review");

        Response updateStatusResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .header("Content-Type", "application/json")
                .body(statusPayload.toString())
                .when()
                .put("https://reno-dev.azurewebsites.net/api/project/update-status/" + projectId)
                .then()
                .extract()
                .response();

        System.out.println("🔁 PUT /project/update-status/" + projectId);
        System.out.println("Response Code: " + updateStatusResponse.getStatusCode());
        System.out.println("Response Body: " + updateStatusResponse.getBody().asPrettyString());

        if (updateStatusResponse.getStatusCode() != 200) {
            Assert.fail("❌ Failed to update project status for project ID: " + projectId);
        }

        System.out.println("✅ Project status updated to 'awaiting-review' successfully.");
        proposeToCustomer();
    }


    public void proposeToCustomer() {
        String adminToken = AuthHelper.getAdminToken();

        // Fetch awaiting-review project
        Response response = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("https://reno-dev.azurewebsites.net/api/admin/project-list?total=0&pageSize=20&current=1&status=awaiting-review")
                .then()
                .extract()
                .response();

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.asString());
            JSONArray dataArray = jsonResponse.getJSONArray("data");

            if (dataArray.length() > 0) {
                JSONObject firstProject = dataArray.getJSONObject(0);

                int proposalId = firstProject.getInt("id");
                int projectId = firstProject.getInt("project_id");

                System.out.println("✅ Found Project:");
                System.out.println("Proposal ID: " + proposalId);
                System.out.println("Project ID: " + projectId);

                // Call get-projects API
                Response getProjectResponse = given()
                        .header("Authorization", "Bearer " + adminToken)
                        .when()
                        .get("https://reno-dev.azurewebsites.net/api/project/get-projects?proposal_id=" + proposalId)
                        .then()
                        .extract()
                        .response();

                System.out.println("✅ GET /get-projects Response:\n" + getProjectResponse.asPrettyString());

                // ✅GET call for OTP Trigger API
                Response otpResponse = given()
                        .header("Authorization", "Bearer " + adminToken)
                        .when()
                        .get("https://reno-dev.azurewebsites.net/api/project/approve-proposal-otp")
                        .then()
                        .extract()
                        .response();

                System.out.println("✅ OTP Trigger Response: " + otpResponse.asPrettyString());

                if (otpResponse.statusCode() == 200) {
                    // Fetch OTP from DB
                    String otp = OTPHelper.fetchLatestOTPFromDB();
                    System.out.println("✅ OTP Fetched from DB: " + otp);

                    // Approve project using OTP
                    JSONObject putPayload = new JSONObject();
                    putPayload.put("project_id", projectId);
                    putPayload.put("status", "approve");
                    putPayload.put("approved_by", "sarthak.bansal@renohome.ae");
                    putPayload.put("otp", otp);

                    Response putResponse = given()
                            .header("Authorization", "Bearer " + adminToken)
                            .header("Content-Type", "application/json")
                            .body(putPayload.toString())
                            .when()
                            .put("https://reno-dev.azurewebsites.net/api/project/status")
                            .then()
                            .extract()
                            .response();

                    System.out.println("✅ PUT /status Response:\n" + putResponse.asPrettyString());

                    if (putResponse.statusCode() == 200) {
                        System.out.println("🎉 Project approved successfully!");
                    } else {
                        System.out.println("❌ Failed to approve project.");
                    }
                } else {
                    System.out.println("❌ OTP trigger failed.");
                }
            } else {
                System.out.println("❌ No 'awaiting-review' projects found.");
            }
        } else {
            System.out.println("❌ Failed to fetch projects.");
        }
        proposedToClientContractSign();
    }
    public void proposedToClientContractSign() {
        String adminToken = AuthHelper.getAdminToken();

        // Fetch proposed-to-client project
        Response response = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("https://reno-dev.azurewebsites.net/api/admin/project-list?total=0&pageSize=20&current=1&status=proposed-to-client")
                .then()
                .extract()
                .response();

        JSONObject jsonResponse = new JSONObject(response.asString());
        JSONArray dataArray = jsonResponse.getJSONArray("data");

        if (dataArray.length() > 0) {
            int projectId = dataArray.getJSONObject(0).getInt("project_id");
            int proposalId = dataArray.getJSONObject(0).getInt("id");
            double proposalCost = dataArray.getJSONObject(0).getDouble("proposal_cost");
            int ClintID = dataArray.getJSONObject(0).getInt("client_id");
            System.out.println("✅ Project ID Found: " + projectId);
            System.out.println("✅ Proposal ID Found: " + proposalId);
            System.out.println("✅ Proposal Cost Found: " + proposalCost);
            System.out.println("✅ Clint ID : " + ClintID);

            // Call Get Project Details API
            Response projectDetailsResponse = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("https://reno-dev.azurewebsites.net//api/project/get-project-details?project_id=" + projectId)
                    .then()
                    .extract()
                    .response();

            System.out.println("✅ Project Details Response: " + projectDetailsResponse.asString());

            // Call Workflow Step API
            Response workflowResponse = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("https://reno-core-api-test.azurewebsites.net/api/v2/steps/workflow/PROJECT_POST_PROPOSAL?refId=" + projectId + "&refType=project")
                    .then()
                    .extract()
                    .response();

            System.out.println("✅ Workflow Step Response: " + workflowResponse.asString());

            // Call KYB Documents API
            Response kybResponse = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("https://reno-dev.azurewebsites.net/api/user/project/" + projectId + "/kyb-documents")
                    .then()
                    .extract()
                    .response();

            System.out.println("✅ KYB Documents Response: " + kybResponse.asString());

            // Call Contract Details
            Response contractDetailsResponse = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("https://reno-core-api-test.azurewebsites.net/api/v2/contracts/details/project/" + projectId)
                    .then()
                    .extract()
                    .response();

            System.out.println("GET /contracts/details/project/" + projectId);
            System.out.println("Response Code: " + contractDetailsResponse.getStatusCode());
            System.out.println("Response Body: " + contractDetailsResponse.getBody().asPrettyString());

            if (contractDetailsResponse.getStatusCode() != 200) {
                Assert.fail("❌ Failed to fetch contract details for project ID: " + projectId);
            }
            // Call Payment Lines using proposalId directly
            Response paymentLinesResponse = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("https://reno-core-api-test.azurewebsites.net/api/v2/paymentlines/proposal/" + proposalId)
                    .then()
                    .extract()
                    .response();

            System.out.println("GET /paymentlines/proposal/" + proposalId);
            System.out.println("Response Code: " + paymentLinesResponse.getStatusCode());
            System.out.println("Response Body: " + paymentLinesResponse.getBody().asPrettyString());

            if (paymentLinesResponse.getStatusCode() != 200) {
                Assert.fail("❌ Failed to fetch payment lines for proposal ID: " + proposalId);
            }

            // Call the Client ID proof
            File signedContract = new File("C:\\Users\\SARTHAK\\IdeaProjects\\API_Automation\\src\\test\\resource\\test-files/NEW.pdf");
            System.out.println("📄 File Exists: " + signedContract.exists());
            if (!signedContract.exists()) {
                System.out.println("❌ Signed contract file does not exist!");
                return;
            }

            try {
                RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
                Response clientIdProofResponse = given()
                        .header("Authorization", "Bearer " + adminToken)
                        .multiPart("proposal_id", String.valueOf(proposalId))
                        .multiPart("signed_contract", signedContract, "application/pdf")
                        .when()
                        .post("https://reno-dev.azurewebsites.net/api/project/client-id-proof")
                        .then()
                        .extract()
                        .response();

                System.out.println("✅ Client ID Proof Response: " + clientIdProofResponse.asString());
            } catch (Exception e) {
                System.out.println("❌ Exception at Client ID Proof API: " + e.getMessage());
                e.printStackTrace();
            }
        // Call the save contract API
            JSONObject contractDetailsPayload = OTPHelper.fetchContractDetailsFromDB();
            contractDetailsPayload.put("projectId", projectId);
            contractDetailsPayload.put("proposalId", proposalId);

            Response Data_response = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .body(contractDetailsPayload.toString())
                    .when()
                    .post("https://reno-core-api-test.azurewebsites.net/api/v2/contracts/details")
                    .then()
                    .extract()
                    .response();

            System.out.println("✅ Contract Details API Response: " + Data_response.asString());

            // OTP Trigger for approve Sign contract
            try {
                Response otpTrigger = given()
                        .header("Authorization", "Bearer " + adminToken)
                        .header("Content-Type", "application/json")
                        .post("https://reno-dev.azurewebsites.net/api/project/approve-contract-otp")
                        .then()
                        .extract().response();

                System.out.println("✅ OTP Triggered: " + otpTrigger.asString());
                // Short wait for DB to update
                Thread.sleep(9000);
                 // OTP Fetch
                String latestOtp = OTPHelper.fetchLatestOTPFromDB();
                System.out.println("✅ OTP from DB: " + latestOtp);
            } catch (Exception e) {
                System.out.println("❌ Exception at Approve Contract OTP API: " + e.getMessage());
                e.printStackTrace();
            }

            String latestProofId = OTPHelper.fetchLatestClientIdProofId();
            String latestOTP = OTPHelper.fetchLatestOTPFromDB();
            if (latestOTP == null || latestOTP.isEmpty()) {
                System.out.println("❌ OTP not found in DB, cannot approve contract.");
                return;
            }
            Response contractApprovalResponse = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType("application/json")
                    .body("{\"otp\":\"" + latestOTP + "\"}")
                    .when()
                    .post("https://reno-dev.azurewebsites.net/api/project/approve-contract/" + latestProofId)
                    .then()
                    .extract()
                    .response();

            System.out.println("✅ Contract Approval Response: " + contractApprovalResponse.asString());
        } else {
            System.out.println("❌ No proposed-to-client project found.");
        }
    }


}
