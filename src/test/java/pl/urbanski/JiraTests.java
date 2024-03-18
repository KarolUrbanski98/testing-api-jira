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
        IssueType issueType = new IssueType();

        project.setKey("APT");
        issueType.setId("10005");

        fields.setProject(project);
        fields.setSummary("Something's wrong");
        fields.setDescription("Very wrong");
        fields.setIssuetype(issueType);

        createIssueReq.setFields(fields);

        RequestSpecification reqAddIssue = request.body(createIssueReq);

        reqAddIssue.when().post("/rest/api/2/issue")
                .then().log().all().assertThat().statusCode(201);
    }
}
