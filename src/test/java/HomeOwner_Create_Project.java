
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
        long randomNumber = System.currentTimeMillis() % 100000;
        String dynamicProjectName = "Project_Number: " + randomNumber;
        // Create the project
        Response projectResponse = given()
                .header("Authorization", "Bearer " + homeOwnerToken)
                .header("X-Account-ID", "")
                .multiPart("name", dynamicProjectName)
                .multiPart("project_type", "Kitchen")
                .multiPart("start_date", "February 03, 2025")
                .multiPart("end_date", "February 24, 2025")
                .multiPart("description", "Test To Check Automation Project Flow")
                .multiPart("exp_id", "2")
                .multiPart("form_json", "{\"appliances\":{\"new_Layouts\":[],\"builtin_appliances\":true,\"new_appliances\":false,\"selected_appliances\":[]},\"kitchenDesignSummary\":{\"data\":{\"projectName\":\"" + dynamicProjectName + "\",\"projectDescription\":\"Test To Check Automation Flow\",\"projectLocation\":\"Dubai\",\"projectSubLocationName\":\"Dubai\",\"projectSubLocation\":\"Dubai\"," +
                        "\"projectType\":\"Kitchen\",\"kitchenLayout\":\"peninsula\",\"size\":\"20\",\"kitchenNewLayout\":[],\"appliances\":[],\"budget\":\"standard\",\"startDate\":\"2025-02-03\",\"endDate\":\"2025-02-24\",\"images\":[],\"builtin_appliances\":true,\"new_appliances\":false," +
                        "\"isLand\":false,\"newLayouts\":false,\"newLighting\":true,\"newFloor\":true,\"cabinets\":true,\"counterTops\":true,\"doorsWindow\":false," +
                        "\"cabinetsWrapping\":true,\"counterTopsWraping\":false}},\"budget_value\":\"3,657\"}")
                .multiPart("layout", "peninsula")
                .multiPart("budget", "standard")
                .multiPart("status", "submitted")
                .post("https://reno-dev.azurewebsites.net/api/project/create-project");


        // ✅ Check project creation response
        System.out.println("Project Response: " + projectResponse.getBody().asString());
        System.out.println("🚀 Submitting Project With Name: " + dynamicProjectName);
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

                proposalId = firstProject.has("proposal_id") ? firstProject.getInt("proposal_id") : -1;
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
                    Assert.fail("❌ API success false — Request not sent to the contractor.");
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
        Add_homeOwner_price();
    }
       public void Add_homeOwner_price() {
            String adminToken = AuthHelper.getAdminToken();

            // Fetch first project with status = awaiting-project-pricing
            Response response = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("https://reno-dev.azurewebsites.net/api/admin/project-list?total=0&pageSize=20&current=1&status=awaiting-project-pricing")
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
                    double totalAmount = firstProject.getDouble("total_amount");

                    System.out.println("Proposal ID: " + proposalId);
                    System.out.println("Project ID: " + projectId);
                    System.out.println("Total Amount: " + totalAmount);

                    // Add 10% to total amount
                    double proposalPrice = round(totalAmount + (totalAmount * 0.10));
                    System.out.println("Proposal Price (Total + 10%): " + proposalPrice);

                    // Call GET API for payment plan
                    Response planResponse = given()
                            .header("Authorization", "Bearer " + adminToken)
                            .when()
                            .get("https://reno-core-api-test.azurewebsites.net/api/v2/payment-plan/proposal/" + proposalId)
                            .then()
                            .extract()
                            .response();

                    System.out.println("Plan GET Response: " + planResponse.asString());

                    // POST API to set proposal view
                    JSONObject postPayload = new JSONObject();
                    postPayload.put("proposalId", proposalId);
                    postPayload.put("proposalPrice", String.valueOf(proposalPrice));
                    postPayload.put("projectPaymentType", "RNPL");
                    postPayload.put("proposalEndDt", "April 19, 2025");

                    JSONArray paymentPlanConfig = new JSONArray();

                    paymentPlanConfig.put(new JSONObject()
                            .put("planType", "MONTHS_3")
                            .put("months", 3)
                            .put("markup", 5)
                            .put("downpayment", 30)
                            .put("moveInPayment", 10));

                    paymentPlanConfig.put(new JSONObject()
                            .put("planType", "MONTHS_6")
                            .put("months", 6)
                            .put("markup", 10)
                            .put("downpayment", 25)
                            .put("moveInPayment", 10));

                    paymentPlanConfig.put(new JSONObject()
                            .put("planType", "MONTHS_12")
                            .put("months", 12)
                            .put("markup", 15)
                            .put("downpayment", 10)
                            .put("moveInPayment", 10));

                    postPayload.put("paymentPlanConfig", paymentPlanConfig);

                    Response postResponse = given()
                            .header("Authorization", "Bearer " + adminToken)
                            .header("Content-Type", "application/json")
                            .body(postPayload.toString())
                            .when()
                            .post("https://reno-core-api-test.azurewebsites.net/api/v2/payment-plan/proposal/" + proposalId + "/view")
                            .then()
                            .extract()
                            .response();

                    System.out.println("POST Response: " + postResponse.asString());

                    // PUT API to save homeowner price
                    JSONObject putPayload = new JSONObject();
                    putPayload.put("proposal_id", proposalId);
                    putPayload.put("amount", String.valueOf(proposalPrice));
                    putPayload.put("payment_type", "RNPL");
                    putPayload.put("show_price", false);
                    putPayload.put("payment_plan", paymentPlanConfig);

                    Response putResponse = given()
                            .header("Authorization", "Bearer " + adminToken)
                            .header("Content-Type", "application/json")
                            .body(putPayload.toString())
                            .when()
                            .put("https://reno-dev.azurewebsites.net/api/project/price")
                            .then()
                            .extract()
                            .response();

                    System.out.println("PUT Response: " + putResponse.asString());

                    if (putResponse.statusCode() == 200) {
                        System.out.println("✅ Homeowner price saved successfully!");
                    } else {
                        System.out.println("❌ Failed to save homeowner price.");
                    }

                    // Call get-projects API
                    fetchProjectDetailsByProposalId(String.valueOf(proposalId), projectId);

                } else {
                    System.out.println("❌ No projects found with status awaiting-project-pricing.");
                }
            } else {
                System.out.println("❌ Failed to fetch project list.");
            }
        }
        public void fetchProjectDetailsByProposalId(String proposalId, int projectId) {
            String adminToken = AuthHelper.getAdminToken();

            Response response = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("https://reno-dev.azurewebsites.net/api/project/get-projects?proposal_id=" + proposalId)
                    .then()
                    .extract()
                    .response();

            System.out.println("GET /project/get-projects?proposal_id=" + proposalId);
            System.out.println("Response Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody().asPrettyString());

            if (response.getStatusCode() == 200) {
                System.out.println("✅ Project details fetched successfully from Admin side!");
            } else {
                Assert.fail("❌ Failed to fetch project details for proposal ID: " + proposalId);
            }
            JSONObject statusPayload = new JSONObject();
            statusPayload.put("status", "awaiting-review");

            Response statusUpdateResponse = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .body(statusPayload.toString())
                    .when()
                    .put("https://reno-dev.azurewebsites.net/api/project/update-status/" + projectId)
                    .then()
                    .extract()
                    .response();

            System.out.println("PUT /project/update-status/" + projectId);
            System.out.println("Response Code: " + statusUpdateResponse.getStatusCode());
            System.out.println("Response Body: " + statusUpdateResponse.getBody().asPrettyString());

            if (statusUpdateResponse.statusCode() == 200) {
                System.out.println("✅ Project status updated to 'awaiting-review'.");
            } else {
                System.out.println("❌ Failed to update project status.");
            }
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
            int projectId = dataArray.getJSONObject(0).getInt("id");
            int proposalId = dataArray.getJSONObject(0).getInt("proposal_id");
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
            JSONObject saveContractPayload = new JSONObject();
            saveContractPayload.put("customerName", "Sarthak Bansal");
            saveContractPayload.put("customerAddress", "Dubai");
            saveContractPayload.put("customerType", "Individual");
            saveContractPayload.put("amount", proposalCost);
            saveContractPayload.put("projectId", projectId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            saveContractPayload.put("signedDate", LocalDate.now().format(formatter));
            saveContractPayload.put("userId", ClintID);

            Response saveContractResponse = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .body(saveContractPayload.toString())
                    .when()
                    .post("https://reno-core-api-test.azurewebsites.net/api/v2/contracts/save")
                    .then()
                    .extract()
                    .response();

            System.out.println("✅ Save Contract Response: " + saveContractResponse.asString());
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
