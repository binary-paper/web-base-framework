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
package net.binarypaper.webbaseframework.rest.utils;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.annotation.Annotation;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.api.validation.ViolationReport;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Assert;

/**
 * Helper class used to by Arquillian REST integration tests
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
public class RestTestHelper {

    private static final String SERVER_URL = "http://localhost:8180/auth";
    private static final String REALM = "demo";
    private static final String CLIENT_ID = "web-base-framework-swagger";
    private static final String CLIENT_SECRET = "235734b5-6067-40e2-aa8d-e187828a4e58";
    private static final String USER_NAME = "test";
    private static final String PASSWORD = "test";

    /**
     * Returns a Keycloak authentication token that will be used by REST service
     * calls for the authentication header
     *
     * @return A Keycloak authentication token
     */
    public static KeycloakToken getKeycloakToken() {
        Form form = new Form();
        form.param("grant_type", "password");
        form.param("client_id", CLIENT_ID);
        form.param("client_secret", CLIENT_SECRET);
        form.param("username", USER_NAME);
        form.param("password", PASSWORD);
        try {
            Response response = ResteasyClientBuilder.newClient()
              .target(SERVER_URL)
              .path("realms")
              .path(REALM)
              .path("protocol")
              .path("openid-connect")
              .path("token")
              .request(MediaType.APPLICATION_JSON)
              .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
            String tokenString = response.readEntity(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            KeycloakToken keycloakToken;
            keycloakToken = objectMapper.readValue(tokenString, KeycloakToken.class);
            return keycloakToken;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Log out a specified
     * 0.Keycloak authentication token
     *
     * @param keycloakToken The Keycloak token to log out
     */
    public static void logoutKeycloakToken(KeycloakToken keycloakToken) {
        Form form = new Form();
        form.param("client_id", CLIENT_ID);
        form.param("refresh_token", keycloakToken.getRefreshToken());
        try {
            Response response = ResteasyClientBuilder.newClient()
              .target(SERVER_URL)
              .path("realms")
              .path(REALM)
              .path("protocol")
              .path("openid-connect")
              .path("logout")
              .queryParam("session_state", keycloakToken.getSessionState())
              .request(MediaType.APPLICATION_JSON)
              .header("Authorization", "Bearer " + keycloakToken.getAccessToken())
              .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        } catch (Exception ex) {
        }
    }

    /**
     * Gets an array of annotations containing a single @JsonView annotation
     * that will be used when marshaling entity objects to JSON and unmarshaling
     * JSON to an entity class.
     * <p>
     * The jsonViewClass must be one of the classes used as the value of the
     *
     * @JsonView annotation that the entity class is annotated with
     *
     * @param jsonViewClass The class used as the value of the @JsonView
     * annotation
     * @return An array of annotations containing a @JsonView annotation
     */
    public static Annotation[] getJsonViewAnnotations(Class jsonViewClass) {
        Annotation[] annotations = new Annotation[1];
        Annotation jsonView = new JsonView() {
            @Override
            public Class<?>[] value() {
                Class[] classes = new Class[1];
                classes[0] = jsonViewClass;
                return classes;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return JsonView.class;
            }
        };
        annotations[0] = jsonView;
        return annotations;
    }

    /**
     * Helper method to count the number of violations of various types
     *
     * @param violationReport The ViolationReport object containing various
     * types of violations
     * @param fieldCount The expected count of field violations
     * @param propertyCount The expected count of property violations
     * @param classCount The expected count of class violations
     * @param parameterCount The expected count of parameter violations
     * @param returnValueCount The expected count of return value violations
     */
    public static void countViolations(ViolationReport violationReport, int fieldCount, int propertyCount, int classCount, int parameterCount, int returnValueCount) {
        Assert.assertEquals(fieldCount, violationReport.getFieldViolations().size());
        Assert.assertEquals(propertyCount, violationReport.getPropertyViolations().size());
        Assert.assertEquals(classCount, violationReport.getClassViolations().size());
        Assert.assertEquals(parameterCount, violationReport.getParameterViolations().size());
        Assert.assertEquals(returnValueCount, violationReport.getReturnValueViolations().size());
    }
}
