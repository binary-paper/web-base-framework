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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.security.Principal;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.java.Log;
import net.binarypaper.webbaseframework.ejb.email.EmailMessage;
import net.binarypaper.webbaseframework.ejb.render.FreeMarkerRenderBean;
import net.binarypaper.webbaseframework.rest.BusinessLogicException;
import net.binarypaper.webbaseframework.rest.ResponseError;
import net.binarypaper.webbaseframework.rest.SwaggerBootstrap;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;

/**
 * Render Notification REST Web Service
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// EJB annotations
@Stateless
// JAX-RS annotations
@Path("render_notification")
// Security annotations
@RolesAllowed("render_notifications")
// Swagger annotations
@Api(value = "Render Notification", authorizations = {
    @Authorization(SwaggerBootstrap.O_AUTH_2)
})
// Lombok annotations
@Log
public class RenderNotificationResource {

    @EJB
    private FreeMarkerRenderBean freeMarkerRenderBean;

    @Inject
    private JMSContext jmsContext;

    @Resource(mappedName = "java:/jms/queue/EmailQueue")
    private Queue emailQueue;

    @Resource
    private SessionContext sessionContext;
    
    @Context
    private ServletContext servletContext;

    // JAX-RS annotations
    @Path("{templateName}")
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.TEXT_HTML, MediaType.TEXT_PLAIN})
    // Swagger annotations
    @ApiOperation(value = "Render a document using the specified template name data",
      notes = "<p>Render a document using the specified template name data</p>"
      + "<p>The REST service can output a rendered document in HTML or plain text format</p>"
      + "<p>The REST service can consume input data in JSON or XML format</p>"
      + "<p>The rendered document can optionally be emailed if a to email addess and subject line is specified</p>",
      response = String.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "The input data is invalid", response = ResponseError.class),
        @ApiResponse(code = 403, message = "Not authorized to call the api"),
        @ApiResponse(code = 404, message = "The template name is invalid", response = ResponseError.class)
    })
    public Response renderDocument(
      @PathParam("templateName")
      @ApiParam(value = "The name of the template file to use for rendering")
      final String templateName,
      @HeaderParam("content-type")
      @ApiParam(hidden = true)
      final String contentType,
      @QueryParam("email_to")
      @ApiParam(value = "The comma separated list of email addresses")
      final String emailTo,
      @QueryParam("email_subject")
      @ApiParam(value = "The email subject of the email")
      final String emailSubject,
      String data) throws BusinessLogicException {
        // Render the output document
        String renderedDocument = freeMarkerRenderBean.render(servletContext, templateName, contentType, data);
        if ((emailTo != null) && (emailSubject != null)) {
            Principal principal = sessionContext.getCallerPrincipal();
            String userName = principal.getName();
            if (principal instanceof KeycloakPrincipal) {
                KeycloakPrincipal<KeycloakSecurityContext> keycloakPrincipal = (KeycloakPrincipal<KeycloakSecurityContext>) principal;
                userName = keycloakPrincipal.getKeycloakSecurityContext().getToken().getPreferredUsername();
            }
            EmailMessage emailMessage = new EmailMessage();
            emailMessage.setUserName(userName);
            emailMessage.setToAddress(emailTo);
            emailMessage.setSubject(emailSubject);
            emailMessage.setBody(renderedDocument);
            ObjectMessage message = jmsContext.createObjectMessage(emailMessage);
            jmsContext.createProducer().send(emailQueue, message);
        }
        return Response.ok(renderedDocument).build();
    }

}
