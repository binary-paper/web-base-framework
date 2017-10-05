/*
 * Copyright 2016 <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.binarypaper.webbaseframework.rest.notification;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.io.File;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.binarypaper.webbaseframework.ejb.email.EmailMessage;
import net.binarypaper.webbaseframework.ejb.render.FreeMarkerRenderBean;
import net.binarypaper.webbaseframework.rest.BusinessLogicException;
import net.binarypaper.webbaseframework.rest.ResponseError;
import net.binarypaper.webbaseframework.rest.utils.KeycloakToken;
import net.binarypaper.webbaseframework.rest.utils.RestTestHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for the RenderNotificationResource REST service.
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
@RunWith(Arquillian.class)
public class RenderNotificationResourceIT {

    private static KeycloakToken KEYCLOAK_TOKEN;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        // Import Maven runtime dependencies
        File[] files = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .withTransitivity()
                .asFile();
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsLibraries(files)
                .addPackage(BusinessLogicException.class.getPackage())
                .addPackage(RenderNotificationResource.class.getPackage())
                .addPackage(FreeMarkerRenderBean.class.getPackage())
                .addPackage(EmailMessage.class.getPackage())
                .addAsResource("ValidationMessages.properties")
                .addAsResource("ErrorMessages.properties")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("WEB-INF/web.xml", "web.xml");
        for (File file : new File("src/main/webapp/WEB-INF/email_templates").listFiles()) {
            war.addAsWebInfResource(file, "/email_templates/" + file.getName());
        }
        return war;
    }

    @BeforeClass
    public static void setUpClass() {
        KEYCLOAK_TOKEN = RestTestHelper.getKeycloakToken();
    }

    @AfterClass
    public static void tearDownClass() {
        RestTestHelper.logoutKeycloakToken(KEYCLOAK_TOKEN);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    @InSequence(1)
    public void notAuthenticated(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Call a REST method without passing the KEYCLOAK_TOKEN
        Response response = webTarget
                .path("/render_notification")
                .path("BasicJSON.html")
                .request(MediaType.TEXT_HTML)
                .post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        response.close();
        Assert.assertNotNull("Ensure that the Keycloak server is started and correctly configured", KEYCLOAK_TOKEN);
        Assert.assertNotNull(KEYCLOAK_TOKEN.getAccessToken());
    }

    @Test
    @InSequence(2)
    public void renderJsonToHtml(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        String inputJson = "{'firstName': 'Albert', 'surname': 'Einstein'}";
        Response response = webTarget
                .path("/render_notification")
                .path("BasicJSON.html")
                .request(MediaType.TEXT_HTML)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String outputHTML = response.readEntity(String.class);
        Assert.assertTrue(outputHTML.contains("Hi Albert Einstein,"));
        response.close();
    }

    @Test
    @InSequence(2)
    public void renderXmlToHtml(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        String inputXml = "<root><firstName>Isaac</firstName><surname>Newton</surname></root>";
        Response response = webTarget
                .path("/render_notification")
                .path("BasicXML.html")
                .request(MediaType.TEXT_HTML)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputXml, MediaType.APPLICATION_XML_TYPE));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String outputHTML = response.readEntity(String.class);
        Assert.assertTrue(outputHTML.contains("Hi Isaac Newton,"));
        response.close();
    }

    @Test
    @InSequence(2)
    public void renderJsonToPlainText(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        String inputJson = "{'firstName': 'Thomas', 'surname': 'Edison'}";
        Response response = webTarget
                .path("/render_notification")
                .path("BasicJSON.txt")
                .request(MediaType.TEXT_PLAIN)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String outputHTML = response.readEntity(String.class);
        Assert.assertTrue(outputHTML.contains("Hello Thomas Edison,"));
        response.close();
    }

    @Test
    @InSequence(2)
    public void renderXmlToPlainText(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        String inputXml = "<root><firstName>Nikola</firstName><surname>Tesla</surname></root>";
        Response response = webTarget
                .path("/render_notification")
                .path("BasicXML.txt")
                .request(MediaType.TEXT_PLAIN)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputXml, MediaType.APPLICATION_XML_TYPE));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String outputHTML = response.readEntity(String.class);
        Assert.assertTrue(outputHTML.contains("Hello Nikola Tesla,"));
        response.close();
    }

    @Test
    @InSequence(3)
    public void renderInvalidTemplate(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        String inputJson = "{'firstName': 'Edwin', 'surname': 'Hubble'}";
        Response response = webTarget
                .path("/render_notification")
                .path("InvalidTemplate.html")
                .request(MediaType.TEXT_HTML)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("FMR2", responseError.getErrorCode());
        Assert.assertEquals("The FreeMarker template could not be parsed", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(3)
    public void renderDataElementNotProvided(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // The surname element is not provided in the input JSON
        String inputJson = "{'firstName': 'Albert'}";
        Response response = webTarget
                .path("/render_notification")
                .path("BasicJSON.html")
                .request(MediaType.TEXT_HTML)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("FMR3", responseError.getErrorCode());
        Assert.assertEquals("The template contains a reference to a data element that is not provided", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(3)
    public void renderInvalidTemplateName(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        String inputJson = "{'firstName': 'Albert', 'surname': 'Einstein'}";
        Response response = webTarget
                .path("/render_notification")
                .path("InvalidTemplate.html")
                .request(MediaType.TEXT_HTML)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("FMR4", responseError.getErrorCode());
        Assert.assertEquals("The template name is invalid", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(3)
    public void renderFreeMarkerRuntimeError(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        String inputJson = "Invalid JSON will cause FreeMarker runtime error";
        Response response = webTarget
                .path("/render_notification")
                .path("BasicJSON.html")
                .request(MediaType.TEXT_HTML)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("FMR5", responseError.getErrorCode());
        Assert.assertTrue(responseError.getMessage().startsWith("A FreeMarker runtime error occurred."));
        response.close();
    }

    @Test
    @InSequence(3)
    public void renderInputXmlInvalid(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        String inputXml = "Invalid XML";
        Response response = webTarget
                .path("/render_notification")
                .path("BasicXML.html")
                .request(MediaType.TEXT_HTML)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputXml, MediaType.APPLICATION_XML_TYPE));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("FMR6", responseError.getErrorCode());
        Assert.assertEquals("The input XML data is invalid", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(4)
    public void renderJsonToHtmlAndEmail(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        String inputJson = "{'firstName': 'Johannes', 'surname': 'Kepler'}";
        Response response = webTarget
                .path("/render_notification")
                .path("BasicJSON.html")
                .queryParam("email_to", "test@example.com")
                .queryParam("email_subject", "Test JSON to HTML Email")
                .request(MediaType.TEXT_HTML)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String outputHTML = response.readEntity(String.class);
        Assert.assertTrue(outputHTML.contains("Hi Johannes Kepler,"));
        response.close();
        // Wait for max 5s for 1 email to arrive
        // WaitForIncomingEmail() is useful if you're sending stuff asynchronously in a separate thread
        Assert.assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        MimeMessage[] emails = greenMail.getReceivedMessages();
        Assert.assertEquals(1, emails.length);
        Assert.assertEquals("Test JSON to HTML Email", emails[0].getSubject());
        Assert.assertTrue(GreenMailUtil.getBody(emails[0]).contains("Hi Johannes Kepler,"));
    }

    @Test
    @InSequence(5)
    public void renderXmlToHtmlAndEmail(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        String inputXml = "<root><firstName>Galileo</firstName><surname>Galilei</surname></root>";
        Response response = webTarget
                .path("/render_notification")
                .path("BasicXML.html")
                .queryParam("email_to", "test@example.com")
                .queryParam("email_subject", "Test XML to HTML Email")
                .request(MediaType.TEXT_HTML)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputXml, MediaType.APPLICATION_XML_TYPE));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String outputHTML = response.readEntity(String.class);
        Assert.assertTrue(outputHTML.contains("Hi Galileo Galilei,"));
        response.close();
        // Wait for max 5s for 1 email to arrive
        // WaitForIncomingEmail() is useful if you're sending stuff asynchronously in a separate thread
        Assert.assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        MimeMessage[] emails = greenMail.getReceivedMessages();
        Assert.assertEquals(1, emails.length);
        Assert.assertEquals("Test XML to HTML Email", emails[0].getSubject());
        Assert.assertTrue(GreenMailUtil.getBody(emails[0]).contains("Hi Galileo Galilei,"));
    }

    @Test
    @InSequence(6)
    public void renderJsonToPlainTextAndEmail(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        String inputJson = "{'firstName': 'Charles', 'surname': 'Darwin'}";
        Response response = webTarget
                .path("/render_notification")
                .path("BasicJSON.txt")
                .queryParam("email_to", "test@example.com")
                .queryParam("email_subject", "Test JSON to Plain Text Email")
                .request(MediaType.TEXT_PLAIN)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputJson, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String outputHTML = response.readEntity(String.class);
        Assert.assertTrue(outputHTML.contains("Hello Charles Darwin,"));
        response.close();
        // Wait for max 5s for 1 email to arrive
        // WaitForIncomingEmail() is useful if you're sending stuff asynchronously in a separate thread
        Assert.assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        MimeMessage[] emails = greenMail.getReceivedMessages();
        Assert.assertEquals(1, emails.length);
        Assert.assertEquals("Test JSON to Plain Text Email", emails[0].getSubject());
        Assert.assertTrue(GreenMailUtil.getBody(emails[0]).contains("Hello Charles Darwin,"));
    }

    @Test
    @InSequence(7)
    public void renderXmlToPlainTextAndEmail(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        String inputXml = "<root><firstName>Marie</firstName><surname>Curie</surname></root>";
        Response response = webTarget
                .path("/render_notification")
                .path("BasicXML.txt")
                .queryParam("email_to", "test@example.com")
                .queryParam("email_subject", "Test XML to Plain Text Email")
                .request(MediaType.TEXT_PLAIN)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(inputXml, MediaType.APPLICATION_XML_TYPE));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String outputHTML = response.readEntity(String.class);
        Assert.assertTrue(outputHTML.contains("Hello Marie Curie,"));
        response.close();
        // Wait for max 5s for 1 email to arrive
        // WaitForIncomingEmail() is useful if you're sending stuff asynchronously in a separate thread
        Assert.assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        MimeMessage[] emails = greenMail.getReceivedMessages();
        Assert.assertEquals(1, emails.length);
        Assert.assertEquals("Test XML to Plain Text Email", emails[0].getSubject());
        Assert.assertTrue(GreenMailUtil.getBody(emails[0]).contains("Hello Marie Curie,"));
    }
}
