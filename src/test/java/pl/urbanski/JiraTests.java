package pl.urbanski;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class JiraTests {

    public RequestSpecification request;

    @BeforeClass
    public void loginByCookie() {
        request = new RequestSpecBuilder().setBaseUri("http://localhost:8080").setContentType(ContentType.JSON).build();

        AuthCookieReq authCookieReq = new AuthCookieReq();
        authCookieReq.setUsername("karol");
        authCookieReq.setPassword("Karol123@");
        RequestSpecification reqCookieBasedAuth = given().log().all().spec(request).body(authCookieReq);

        AuthCookieRes authCookieRes = reqCookieBasedAuth.when().post("/rest/auth/1/session")
                .then().log().all().extract().response().as(AuthCookieRes.class);

        String jSessionId = authCookieRes.getSession().getValue();
        request = given().log().all().spec(request).cookie("JSESSIONID", jSessionId);
    }

    @Test
    public void testCreatingIssue() {
        CreateIssueReq createIssueReq = new CreateIssueReq();
        CreateIssueReq.Fields fields = new CreateIssueReq.Fields();
        Project project = new Project();
        Issuetype issuetype = new Issuetype();

        project.setKey("APT");
        fields.setProject(project);

        /*
        issue types:
            10001 - Epic
            10002 - Story
            10003 - Task
            10005 - Bug
         */

        issuetype.setId("10005");
        fields.setIssuetype(issuetype);

        fields.setSummary("Something's wrong");
        fields.setDescription("Very wrong");

        createIssueReq.setFields(fields);

        request.body(createIssueReq).when().post("/rest/api/2/issue")
                .then().log().all().assertThat().statusCode(201);
    }

    @Test
    public void testDeletingIssue() {
        String issueId = "10005";

        request.pathParam("issueId", issueId)
                .when().delete("/rest/api/2/issue/{issueId}")
                .then().log().all().assertThat().statusCode(204);
    }

    @Test
    public void testAddingComment() {
        String issueId = "10015";
        String expectedMessage = "Hi! Adding a comment.";
        CommentReq commentReq = new CommentReq();
        Visibility visibility = new Visibility();

        visibility.setType("role");
        visibility.setValue("Administrators");
        commentReq.setVisibility(visibility);

        commentReq.setBody(expectedMessage);

        request.pathParam("issueId", issueId).body(commentReq)
                .when().post("/rest/api/2/issue/{issueId}/comment")
                .then().log().all().assertThat().statusCode(201);
    }

    @Test
    public void testUpdatingComment() {
        String issueId = "10015";
        String commentId = "10005";
        CommentReq commentReq = new CommentReq();
        Visibility visibility = new Visibility();

        visibility.setType("role");
        visibility.setValue("Administrators");
        commentReq.setVisibility(visibility);

        commentReq.setBody("Updating the comment.");

        request.pathParam("issueId", issueId).pathParam("commentId", commentId).body(commentReq)
                .when().put("/rest/api/2/issue/{issueId}/comment/{commentId}")
                .then().log().all().assertThat().statusCode(200);
    }
}
