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
package net.binarypaper.webbaseframework.rest.lookup;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import net.binarypaper.webbaseframework.entity.ActivatableEntity;
import net.binarypaper.webbaseframework.entity.AuditRevision;
import net.binarypaper.webbaseframework.entity.lookup.LookupValue;
import net.binarypaper.webbaseframework.rest.BusinessLogicException;
import net.binarypaper.webbaseframework.rest.ResponseError;
import net.binarypaper.webbaseframework.rest.utils.KeycloakToken;
import net.binarypaper.webbaseframework.rest.utils.RestTestHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ViolationReport;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for the LookupValueResource REST service.
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
@RunWith(Arquillian.class)
public class LookupValueResourceIT {

    private static final GenericType<List<LookupValue>> LOOKUP_VALUE_GENERIC_TYPE = new GenericType<List<LookupValue>>() {
    };
    private static KeycloakToken KEYCLOAK_TOKEN;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        // Import Maven runtime dependencies
        File[] files = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .withTransitivity()
                .asFile();
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsLibraries(files)
                .addPackage(ActivatableEntity.class.getPackage())
                .addPackage(BusinessLogicException.class.getPackage())
                .addPackage(LookupValue.class.getPackage())
                .addPackage(LookupValueResource.class.getPackage())
                .addAsResource("ValidationMessages.properties")
                .addAsResource("ErrorMessages.properties")
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("WEB-INF/web.xml", "web.xml");
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
                .path("/lookup_values/lookup_list_name")
                .path("vehicle_make")
                .request(MediaType.APPLICATION_JSON)
                .get();
        Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
        response.close();
        Assert.assertNotNull("Ensure that the Keycloak server is started and correctly configured", KEYCLOAK_TOKEN);
        Assert.assertNotNull(KEYCLOAK_TOKEN.getAccessToken());
    }

    @Test
    @InSequence(2)
    public void addLookupValueFord(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add vehicle_make Ford
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_make");
        lookupValue.setDisplayValue("Ford");
        lookupValue.setActive(Boolean.TRUE);
        Response response = webTarget
                .path("/lookup_values")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        Assert.assertEquals(1L, lookupValue.getId().longValue());
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
        response.close();
    }

    @Test
    @InSequence(3)
    public void addLookupValueVW(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add vehicle_make VW
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_make");
        lookupValue.setDisplayValue("VW");
        lookupValue.setActive(Boolean.TRUE);
        Response response = webTarget
                .path("/lookup_values")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        Assert.assertEquals(2L, lookupValue.getId().longValue());
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
        response.close();
    }

    @Test
    @InSequence(4)
    public void addLookupValueFocus(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add vehicle_model Focus under Ford
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_model");
        lookupValue.setDisplayValue("Focus");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(1L);
        Response response = webTarget
                .path("/lookup_values")
                .queryParam("parent_id", 1L)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        Assert.assertEquals(3L, lookupValue.getId().longValue());
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
        response.close();
    }

    @Test
    @InSequence(5)
    public void addLookupValueEscort(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add vehicle_model Escort under Ford that is inactive
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_model");
        lookupValue.setDisplayValue("Escort");
        lookupValue.setActive(Boolean.FALSE);
        lookupValue.setParentId(1L);
        Response response = webTarget
                .path("/lookup_values")
                .queryParam("parent_id", 1L)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        Assert.assertEquals(4L, lookupValue.getId().longValue());
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
        Assert.assertEquals(Boolean.FALSE, lookupValue.getActive());
        response.close();
    }

    @Test
    @InSequence(6)
    public void addLookupValueSierra(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add vehicle_model Sierra under Ford that is active
        // and has an effective from and effective to date
        Date effectiveFrom = (new GregorianCalendar(2016, Calendar.JANUARY, 1)).getTime();
        Date effectiveTo = (new GregorianCalendar(2016, Calendar.DECEMBER, 31)).getTime();
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_model");
        lookupValue.setDisplayValue("Sierra");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setEffectiveFrom(effectiveFrom);
        lookupValue.setEffectiveTo(effectiveTo);
        lookupValue.setParentId(1L);
        Response response = webTarget
                .path("/lookup_values")
                .queryParam("parent_id", 1L)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        Assert.assertEquals(5L, lookupValue.getId().longValue());
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
        Assert.assertEquals(Boolean.TRUE, lookupValue.getActive());
        Assert.assertEquals(effectiveFrom, lookupValue.getEffectiveFrom());
        Assert.assertEquals(effectiveTo, lookupValue.getEffectiveTo());
        response.close();
    }

    @Test
    @InSequence(7)
    public void addLookupValuePolo(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add vehicle_model Polo under VW
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_model");
        lookupValue.setDisplayValue("Polo");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(2L);
        Response response = webTarget
                .path("/lookup_values")
                .queryParam("parent_id", 2L)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        Assert.assertEquals(6L, lookupValue.getId().longValue());
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
        response.close();
    }

    @Test
    @InSequence(8)
    public void addLookupValueParentIdQueryParamNull(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add a lookup value with a parentId in the query param, but no parentId in the request body
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_model");
        lookupValue.setDisplayValue("Focus");
        lookupValue.setActive(Boolean.TRUE);
        Response response = webTarget
                .path("/lookup_values")
                .queryParam("parent_id", 1L)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0001", responseError.getErrorCode());
        Assert.assertEquals("The parent lookup value id in the URL does not match the parent id in the request body", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(8)
    public void addLookupValueParentIdBodyNull(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add a lookup value with a parentId in the query param, but a different parentId in the request body
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_model");
        lookupValue.setDisplayValue("Focus");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(2L);
        Response response = webTarget
                .path("/lookup_values")
                .queryParam("parent_id", 1L)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0001", responseError.getErrorCode());
        Assert.assertEquals("The parent lookup value id in the URL does not match the parent id in the request body", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(8)
    public void addLookupValueParentIdNotMatching(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add a lookup value with a parentId in the request body, but no parentId in the query param
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_model");
        lookupValue.setDisplayValue("Focus");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(1L);
        Response response = webTarget
                .path("/lookup_values")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0001", responseError.getErrorCode());
        Assert.assertEquals("The parent lookup value id in the URL does not match the parent id in the request body", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(8)
    public void addLookupValueParentIdInvalid(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add a lookup value with a parentId that is invalid
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_model");
        lookupValue.setDisplayValue("Focus");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(100L);
        Response response = webTarget
                .path("/lookup_values")
                .queryParam("parent_id", 100L)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0002", responseError.getErrorCode());
        Assert.assertEquals("The specified parent id is invalid", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(8)
    public void addLookupValueLookupListNameSameAsParent(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add a lookup value with the same lookup list name as its parent
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_make");
        lookupValue.setDisplayValue("Toyota");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(1L);
        Response response = webTarget
                .path("/lookup_values")
                .queryParam("parent_id", 1L)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0003", responseError.getErrorCode());
        Assert.assertEquals("A lookup value may not have the same lookup list name as its parent lookup value", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(8)
    public void addLookupValueParentIdNullUCViolation(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add a lookup value without a parentId that would violate the UC_LOOKUP_LIST_VALUE unique constraint
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_make");
        lookupValue.setDisplayValue("Ford");
        lookupValue.setActive(Boolean.TRUE);
        Response response = webTarget
                .path("/lookup_values")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0004", responseError.getErrorCode());
        Assert.assertEquals("The combination of the lookup list name, display value and parent lookup list must be unique", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(8)
    public void addLookupValueParentIdNotNullUCViolation(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add a lookup value with a parentId that would violate the UC_LOOKUP_LIST_VALUE unique constraint
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle_model");
        lookupValue.setDisplayValue("Focus");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(1L);
        Response response = webTarget
                .path("/lookup_values")
                .queryParam("parent_id", 1L)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0004", responseError.getErrorCode());
        Assert.assertEquals("The combination of the lookup list name, display value and parent lookup list must be unique", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(9)
    public void addLookupValueBeanValidationError(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Add a lookup value constraint violation errors
        LookupValue lookupValue = new LookupValue();
        lookupValue.setDisplayValue("Toyota");
        lookupValue.setActive(Boolean.TRUE);
        Response response = webTarget
                .path("/lookup_values")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Add.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ViolationReport violationReport = response.readEntity(ViolationReport.class);
        RestTestHelper.countViolations(violationReport, 0, 1, 0, 0, 0);
        ResteasyConstraintViolation violation = violationReport.getPropertyViolations().get(0);
        Assert.assertEquals("The lookup list name must be specified", violation.getMessage());
        response.close();
    }

    @Test
    @InSequence(10)
    public void getLookupValuesNoContent(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        Response response = webTarget
                .path("/lookup_values/lookup_list_name")
                .path("lookup_list_name")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
        response.close();
    }

    @Test
    @InSequence(10)
    public void getLookupValuesInvalidEffectiveDate(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        Response response = webTarget
                .path("/lookup_values/lookup_list_name")
                .path("vehicle_make")
                .queryParam("effective_date", "2016-01")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0007", responseError.getErrorCode());
        Assert.assertEquals("The effective date is invalid", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(10)
    public void getVehicleMake(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get list of vehicle make
        Response response = webTarget
                .path("/lookup_values/lookup_list_name")
                .path("vehicle_make")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        List<LookupValue> lookupValues = response.readEntity(LOOKUP_VALUE_GENERIC_TYPE,
                RestTestHelper.getJsonViewAnnotations(LookupValue.View.List.class));
        Assert.assertEquals(2, lookupValues.size());
        LookupValue lookupValue = lookupValues.get(0);
        Assert.assertEquals(1L, lookupValue.getId().longValue());
        Assert.assertEquals("Ford", lookupValue.getDisplayValue());
        lookupValue = lookupValues.get(1);
        Assert.assertEquals(2L, lookupValue.getId().longValue());
        Assert.assertEquals("VW", lookupValue.getDisplayValue());
        response.close();
    }

    @Test
    @InSequence(10)
    public void getVehicleModel(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get list of vehicle model
        Response response = webTarget
                .path("/lookup_values/lookup_list_name")
                .path("vehicle_model")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        List<LookupValue> lookupValues = response.readEntity(LOOKUP_VALUE_GENERIC_TYPE,
                RestTestHelper.getJsonViewAnnotations(LookupValue.View.List.class));
        Assert.assertEquals(4, lookupValues.size());
        response.close();
    }

    @Test
    @InSequence(10)
    public void getVehicleModelFord(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get list of vehicle model filtering by parentId 1 (Ford)
        Response response = webTarget
                .path("/lookup_values/lookup_list_name")
                .path("vehicle_model")
                .queryParam("parent_id", 1L)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        List<LookupValue> lookupValues = response.readEntity(LOOKUP_VALUE_GENERIC_TYPE,
                RestTestHelper.getJsonViewAnnotations(LookupValue.View.List.class));
        Assert.assertEquals(3, lookupValues.size());
        response.close();
    }

    @Test
    @InSequence(10)
    public void getVehicleModelFordActive(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get list of vehicle model filtering by parentId 1 (Ford)
        // and active = TRUE
        Response response = webTarget
                .path("/lookup_values/lookup_list_name")
                .path("vehicle_model")
                .queryParam("parent_id", 1L)
                .queryParam("active", Boolean.TRUE)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        List<LookupValue> lookupValues = response.readEntity(LOOKUP_VALUE_GENERIC_TYPE,
                RestTestHelper.getJsonViewAnnotations(LookupValue.View.List.class));
        Assert.assertEquals(2, lookupValues.size());
        response.close();
    }

    @Test
    @InSequence(10)
    public void getVehicleModelFordOnEffectiveFromDate(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get list of vehicle model filtering by parentId 1 (Ford)
        // and active = TRUE and effectiveDate = 2016-01-01
        Response response = webTarget
                .path("/lookup_values/lookup_list_name")
                .path("vehicle_model")
                .queryParam("parent_id", 1L)
                .queryParam("active", Boolean.TRUE)
                .queryParam("effective_date", "2016-01-01")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        List<LookupValue> lookupValues = response.readEntity(LOOKUP_VALUE_GENERIC_TYPE,
                RestTestHelper.getJsonViewAnnotations(LookupValue.View.List.class));
        Assert.assertEquals(2, lookupValues.size());
        response.close();
    }

    @Test
    @InSequence(10)
    public void getVehicleModelFordBeforeEffectiveFromDate(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get list of vehicle model filtering by parentId 1 (Ford)
        // and active = TRUE and effectiveDate = 2015-12-31
        Response response = webTarget
                .path("/lookup_values/lookup_list_name")
                .path("vehicle_model")
                .queryParam("parent_id", 1L)
                .queryParam("active", Boolean.TRUE)
                .queryParam("effective_date", "2015-12-31")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        List<LookupValue> lookupValues = response.readEntity(LOOKUP_VALUE_GENERIC_TYPE,
                RestTestHelper.getJsonViewAnnotations(LookupValue.View.List.class));
        Assert.assertEquals(1, lookupValues.size());
        response.close();
    }

    @Test
    @InSequence(10)
    public void getVehicleModelFordOnEffectiveToDate(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get list of vehicle model filtering by parentId 1 (Ford)
        // and active = TRUE and effectiveDate = 2016-12-31
        Response response = webTarget
                .path("/lookup_values/lookup_list_name")
                .path("vehicle_model")
                .queryParam("parent_id", 1L)
                .queryParam("active", Boolean.TRUE)
                .queryParam("effective_date", "2016-12-31")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        List<LookupValue> lookupValues = response.readEntity(LOOKUP_VALUE_GENERIC_TYPE,
                RestTestHelper.getJsonViewAnnotations(LookupValue.View.List.class));
        Assert.assertEquals(2, lookupValues.size());
        response.close();
    }

    @Test
    @InSequence(10)
    public void getVehicleModelFordAfterEffectivetoDate(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get list of vehicle model filtering by parentId 1 (Ford)
        // and active = TRUE and effectiveDate = 2017-01-01
        Response response = webTarget
                .path("/lookup_values/lookup_list_name")
                .path("vehicle_model")
                .queryParam("parent_id", 1L)
                .queryParam("active", Boolean.TRUE)
                .queryParam("effective_date", "2017-01-01")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        List<LookupValue> lookupValues = response.readEntity(LOOKUP_VALUE_GENERIC_TYPE,
                RestTestHelper.getJsonViewAnnotations(LookupValue.View.List.class));
        Assert.assertEquals(1, lookupValues.size());
        response.close();
    }

    @Test
    @InSequence(10)
    public void getVehicleModelVWActive(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get list of vehicle model filtering by parentId 2 (VW)
        // and active = TRUE
        Response response = webTarget
                .path("/lookup_values/lookup_list_name")
                .path("vehicle_model")
                .queryParam("parent_id", 2L)
                .queryParam("active", Boolean.TRUE)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        List<LookupValue> lookupValues = response.readEntity(LOOKUP_VALUE_GENERIC_TYPE,
                RestTestHelper.getJsonViewAnnotations(LookupValue.View.List.class));
        Assert.assertEquals(1, lookupValues.size());
        response.close();
    }

    @Test
    @InSequence(10)
    public void getVehicleModelVWInActive(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get list of vehicle model filtering by parentId 2 (VW)
        // and active = FALSE
        Response response = webTarget
                .path("/lookup_values/lookup_list_name")
                .path("vehicle_model")
                .queryParam("parent_id", 2L)
                .queryParam("active", Boolean.FALSE)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
        response.close();
    }

    @Test
    @InSequence(11)
    public void getLookupValueFord(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get vehicle make Ford by id 1
        Response response = webTarget
                .path("/lookup_values")
                .path("1")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        LookupValue lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        Assert.assertEquals(1L, lookupValue.getId().longValue());
        Assert.assertEquals("vehicle_make", lookupValue.getLookupListName());
        Assert.assertEquals("Ford", lookupValue.getDisplayValue());
        response.close();
    }

    @Test
    @InSequence(11)
    public void getLookupValueInvalidId(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get lookup value with invalid id
        Response response = webTarget
                .path("/lookup_values")
                .path("7")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
        response.close();
    }

    @Test
    @InSequence(12)
    public void updateLookupValuePolo(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Update Lookup value Polo
        Date effectiveFrom = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
        Date effectiveTo = new GregorianCalendar(2016, Calendar.DECEMBER, 31).getTime();
        Response response = webTarget
                .path("/lookup_values")
                .path("6")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        LookupValue lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        response.close();
        lookupValue.setActive(Boolean.FALSE);
        lookupValue.setEffectiveFrom(effectiveFrom);
        lookupValue.setEffectiveTo(effectiveTo);
        response = webTarget
                .path("/lookup_values")
                .path("6")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .put(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Edit.class)));
        Assert.assertEquals(Status.ACCEPTED.getStatusCode(), response.getStatus());
        lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        Assert.assertEquals(1L, lookupValue.getVersion().longValue());
        Assert.assertEquals(Boolean.FALSE, lookupValue.getActive());
        Assert.assertEquals(effectiveFrom, lookupValue.getEffectiveFrom());
        Assert.assertEquals(effectiveTo, lookupValue.getEffectiveTo());
        response.close();
    }

    @Test
    @InSequence(12)
    public void updateLookupValueInvalidId(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Update lookup value with invalid id
        LookupValue lookupValue = new LookupValue();
        lookupValue.setId(7L);
        lookupValue.setVersion(0L);
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setLookupListName("some_lookup_list");
        lookupValue.setDisplayValue("Some lookup list value");
        Response response = webTarget
                .path("/lookup_values")
                .path("7")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .put(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Edit.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0006", responseError.getErrorCode());
        Assert.assertEquals("The lookup value id is invalid", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(12)
    public void updateLookupValueIdInPathNotMatchIdInBody(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Update lookjup value with id in path not matching id in body
        LookupValue lookupValue = new LookupValue();
        lookupValue.setId(7L);
        lookupValue.setVersion(0L);
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setLookupListName("some_lookup_list");
        lookupValue.setDisplayValue("Some lookup list value");
        Response response = webTarget
                .path("/lookup_values")
                .path("8")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .put(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Edit.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0005", responseError.getErrorCode());
        Assert.assertEquals("The lookup value id in the URL does not match the id in the request body", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(12)
    public void updateLookupValueParentIdNullUCViolation(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Update lookup value with parent id null and create a unique constraint violation
        // Get Vehicle make VW
        Response response = webTarget
                .path("/lookup_values")
                .path("2")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        LookupValue lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        response.close();
        // Change display value VW to Ford
        lookupValue.setDisplayValue("Ford");
        response = webTarget
                .path("/lookup_values")
                .path("2")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .put(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Edit.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0004", responseError.getErrorCode());
        Assert.assertEquals("The combination of the lookup list name, display value and parent lookup list must be unique", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(12)
    public void updateLookupValueParentIdNotNullUCViolation(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Update lookup value with parent id not null and create a unique constraint violation
        // Get Vehicle make Escort
        Response response = webTarget
                .path("/lookup_values")
                .path("4")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        LookupValue lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        response.close();
        // Change display value Escort to Focus
        lookupValue.setDisplayValue("Focus");
        response = webTarget
                .path("/lookup_values")
                .path("4")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .put(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Edit.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0004", responseError.getErrorCode());
        Assert.assertEquals("The combination of the lookup list name, display value and parent lookup list must be unique", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(12)
    public void updateLookupValueConcurrencyIssue(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Update lookup value and create a concurrency issue
        // Get Vehicle make Ford
        Response response = webTarget
                .path("/lookup_values")
                .path("1")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        LookupValue lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        response.close();
        // Change the version from 0 to 1
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
        lookupValue.setVersion(1L);
        response = webTarget
                .path("/lookup_values")
                .path("1")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .put(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Edit.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("F001", responseError.getErrorCode());
        Assert.assertEquals("The entity has been updated since it has been retrieved", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(12)
    public void updateLookupValueNoUpdatableFieldUpdated(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Update lookup value without changing any of the updateable fields
        // Get Vehicle make Ford
        Response response = webTarget
                .path("/lookup_values")
                .path("1")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        LookupValue lookupValue = response.readEntity(LookupValue.class, RestTestHelper.getJsonViewAnnotations(LookupValue.View.All.class));
        response.close();
        // Change the lookup list name which is not updatable from vehicle_make to vehicle_make_updated
        Assert.assertEquals("vehicle_make", lookupValue.getLookupListName());
        lookupValue.setLookupListName("vehicle_make_updated");
        response = webTarget
                .path("/lookup_values")
                .path("1")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .put(Entity.entity(lookupValue, MediaType.APPLICATION_JSON_TYPE, RestTestHelper.getJsonViewAnnotations(LookupValue.View.Edit.class)));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("F002", responseError.getErrorCode());
        Assert.assertEquals("None of the updatable fields were updated", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(13)
    public void deleteLookupValuePolo(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Delete lookup value Polo
        Response response = webTarget
                .path("/lookup_values")
                .path("6")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .delete();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        response.close();
    }

    @Test
    @InSequence(13)
    public void deleteLookupValueWithInvalidId(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Delete lookup value with an invalid id
        Response response = webTarget
                .path("/lookup_values")
                .path("7")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .delete();
        Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0006", responseError.getErrorCode());
        Assert.assertEquals("The lookup value id is invalid", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(13)
    public void deleteLookupValueWithChildren(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Delete lookup value with children
        Response response = webTarget
                .path("/lookup_values")
                .path("1")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .delete();
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0008", responseError.getErrorCode());
        Assert.assertEquals("A lookup value cannot be deleted if it has child lookup values", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(14)
    public void getLookupValueRevisionsPolo(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get lookup value revisions for lookup value Polo
        Response response = webTarget
                .path("/lookup_values")
                .path("6")
                .path("revisions")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        List<LookupValue> lookupValues = response.readEntity(LOOKUP_VALUE_GENERIC_TYPE,
                RestTestHelper.getJsonViewAnnotations(AuditRevision.class));
        Assert.assertEquals(3, lookupValues.size());
        // Get the first lookup value revision
        LookupValue lookupValue = lookupValues.get(0);
        Assert.assertEquals("ADD", lookupValue.getRevision().getRevisionType());
        Assert.assertEquals("test", lookupValue.getRevision().getUserName());
        Assert.assertEquals(Boolean.TRUE, lookupValue.getActive());
        Assert.assertEquals(null, lookupValue.getEffectiveFrom());
        Assert.assertEquals(null, lookupValue.getEffectiveTo());
        // Get the second lookup value revision
        lookupValue = lookupValues.get(1);
        Assert.assertEquals("MOD", lookupValue.getRevision().getRevisionType());
        Assert.assertEquals("test", lookupValue.getRevision().getUserName());
        Assert.assertEquals(Boolean.FALSE, lookupValue.getActive());
        Date effectiveFrom = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
        Date effectiveTo = new GregorianCalendar(2016, Calendar.DECEMBER, 31).getTime();
        Assert.assertEquals(effectiveFrom, lookupValue.getEffectiveFrom());
        Assert.assertEquals(effectiveTo, lookupValue.getEffectiveTo());
        // Get the third lookup value revision
        lookupValue = lookupValues.get(2);
        Assert.assertEquals("DEL", lookupValue.getRevision().getRevisionType());
        Assert.assertEquals("test", lookupValue.getRevision().getUserName());
        Assert.assertEquals(null, lookupValue.getActive());
        Assert.assertEquals(null, lookupValue.getEffectiveFrom());
        Assert.assertEquals(null, lookupValue.getEffectiveTo());
        response.close();
    }

    @Test
    @InSequence(14)
    public void getLookupValueRevisionsInvalidId(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Get lookup value revisions for an invalid lookup value id
        Response response = webTarget
                .path("/lookup_values")
                .path("7")
                .path("revisions")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .get();
        Assert.assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
        response.close();
    }

    @Test
    @InSequence(15)
    public void uploadCsvFileValid(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Upload a valid CSV file
        File csvFile = new File("src/test/resources/CsvUploadValid.csv");
        MultipartFormDataOutput output = new MultipartFormDataOutput();
        output.addFormData("csvFile", new FileInputStream(csvFile), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(output) {
        };
        Response response = webTarget
                .path("/lookup_values/csv_upload")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
        List<LookupValue> lookupValues = response.readEntity(LOOKUP_VALUE_GENERIC_TYPE,
                RestTestHelper.getJsonViewAnnotations(LookupValue.View.List.class));
        Assert.assertEquals(12, lookupValues.size());
        response.close();
    }

    @Test
    @InSequence(16)
    public void uploadCsvFileInvalidPart(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Upload a CSV file with an invalid part
        File csvFile = new File("src/test/resources/CsvUploadValid.csv");
        MultipartFormDataOutput output = new MultipartFormDataOutput();
        output.addFormData("invalidPart", new FileInputStream(csvFile), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(output) {
        };
        Response response = webTarget
                .path("/lookup_values/csv_upload")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0009", responseError.getErrorCode());
        Assert.assertEquals("The multipart/form-data must contain an part called csvFile", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(16)
    public void uploadCsvFileLookupListNameSameAsParent(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Upload a CSV file with lookup list name same as parent
        File csvFile = new File("src/test/resources/CsvUploadLookupListNameSameAsParent.csv");
        MultipartFormDataOutput output = new MultipartFormDataOutput();
        output.addFormData("csvFile", new FileInputStream(csvFile), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(output) {
        };
        Response response = webTarget
                .path("/lookup_values/csv_upload")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0010", responseError.getErrorCode());
        Assert.assertEquals("One of the lookup values in the csv file has the same lookup list name as its parent lookup value", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(16)
    public void uploadCsvFileDuplicate(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Upload a CSV file with duplicate lookup values
        File csvFile = new File("src/test/resources/CsvUploadValid.csv");
        MultipartFormDataOutput output = new MultipartFormDataOutput();
        output.addFormData("csvFile", new FileInputStream(csvFile), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(output) {
        };
        Response response = webTarget
                .path("/lookup_values/csv_upload")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0011", responseError.getErrorCode());
        Assert.assertEquals("At least one of the lookup values in the CSV file already exists", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(16)
    public void uploadCsvFileInvalidDate(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Upload a CSV file with an invalid effective date for a lookup values
        File csvFile = new File("src/test/resources/CsvUploadInvalidDate.csv");
        MultipartFormDataOutput output = new MultipartFormDataOutput();
        output.addFormData("csvFile", new FileInputStream(csvFile), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(output) {
        };
        Response response = webTarget
                .path("/lookup_values/csv_upload")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0013", responseError.getErrorCode());
        Assert.assertEquals("The uploaded CSV file contains an EFFECTIVE_FROM or EFFECTIVE_TO date that is not in the format yyyy-MM-dd", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(16)
    public void uploadCsvFileInvalidParent(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Upload a CSV file with an invalid parent for a lookup values
        File csvFile = new File("src/test/resources/CsvUploadInvalidParent.csv");
        MultipartFormDataOutput output = new MultipartFormDataOutput();
        output.addFormData("csvFile", new FileInputStream(csvFile), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(output) {
        };
        Response response = webTarget
                .path("/lookup_values/csv_upload")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0014", responseError.getErrorCode());
        Assert.assertEquals("One of the records in the CSV file contains a reference to a parent lookup value that does not exist", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(16)
    public void uploadCsvFileAmbiguousParent(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Upload a CSV file with an ambiguous parent for a lookup values
        File csvFile = new File("src/test/resources/CsvUploadAmbiguousParent.csv");
        MultipartFormDataOutput output = new MultipartFormDataOutput();
        output.addFormData("csvFile", new FileInputStream(csvFile), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(output) {
        };
        Response response = webTarget
                .path("/lookup_values/csv_upload")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0015", responseError.getErrorCode());
        Assert.assertEquals("One of the records in the CSV file contains a reference to a parent lookup value that is ambiguous", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(16)
    public void uploadCsvFileIncorrectHeader(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Upload a CSV file with an incorrect header
        File csvFile = new File("src/test/resources/CsvUploadIncorrectHeader.csv");
        MultipartFormDataOutput output = new MultipartFormDataOutput();
        output.addFormData("csvFile", new FileInputStream(csvFile), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(output) {
        };
        Response response = webTarget
                .path("/lookup_values/csv_upload")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ResponseError responseError = response.readEntity(ResponseError.class);
        Assert.assertEquals("0016", responseError.getErrorCode());
        Assert.assertEquals("The CSV file headers are invalid", responseError.getMessage());
        response.close();
    }

    @Test
    @InSequence(16)
    public void uploadCsvFileConstraintViolation(@ArquillianResteasyResource WebTarget webTarget) throws Exception {
        // Upload a CSV file with a bean validation constraint violation
        File csvFile = new File("src/test/resources/CsvUploadConstraintViolation.csv");
        MultipartFormDataOutput output = new MultipartFormDataOutput();
        output.addFormData("csvFile", new FileInputStream(csvFile), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(output) {
        };
        Response response = webTarget
                .path("/lookup_values/csv_upload")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + KEYCLOAK_TOKEN.getAccessToken())
                .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ViolationReport violationReport = response.readEntity(ViolationReport.class);
        RestTestHelper.countViolations(violationReport, 0, 1, 0, 0, 0);
        ResteasyConstraintViolation violation = violationReport.getPropertyViolations().get(0);
        Assert.assertEquals("The lookup list name must be between 3 and 100 characters long", violation.getMessage());
        response.close();
    }
}
