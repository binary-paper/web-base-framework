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
package net.binarypaper.webbaseframework.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.extern.java.Log;
import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.jboss.resteasy.api.validation.ResteasyViolationExceptionMapper;

/**
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// JAX-RS annotations
@Provider
// Lombok annotations
@Log
public class BusinessLogicExceptionMapper implements ExceptionMapper<BusinessLogicException> {

    private static final ResourceBundle ERROR_MESSAGES = ResourceBundle.getBundle("ErrorMessages");

    @Override
    public Response toResponse(BusinessLogicException ex) {
        // Handle BusinessLogicException that has a cause
        if (ex.getCause() != null) {
            // Handle BusinessLogicException where the cause is a ConstraintViolationException
            if (ex.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) ex.getCause();
                List<MediaType> accept = new ArrayList<>();
                accept.add(MediaType.APPLICATION_JSON_TYPE);
                ResteasyViolationException rve = new ResteasyViolationException(cve.getConstraintViolations(), accept);
                ResteasyViolationExceptionMapper rvem = new ResteasyViolationExceptionMapper();
                return rvem.toResponse(rve);
            }
        }
        // Handle BusinessLogicException that has an errorCode
        if ((ex.getErrorCode() != null) && !ex.getErrorCode().isEmpty()) {
            try {
                String message = ERROR_MESSAGES.getString(ex.getErrorCode());
                if ((ex.getMessage() != null) && !ex.getMessage().isEmpty()) {
                    message = message + "\n\n" + ex.getMessage();
                }
                ResponseError error = new ResponseError(ex.getErrorCode(), message);
                return Response
                  .status(ex.getHttpStatusCode())
                  .entity(error)
                  .type(MediaType.APPLICATION_JSON_TYPE)
                  .build();
            } catch (NumberFormatException nfe) {
                // Handle BusinessLogicException where the errorCode has been
                // configured with an HTTP Status Code that is not a valid integer
                String message = String.format("The error code %s in the ErrorHttpCodes.properties "
                  + "file is not an integer", ex.getErrorCode());
                log.severe(message);
                return Response
                  .status(Status.INTERNAL_SERVER_ERROR)
                  .entity(message)
                  .build();
            } catch (MissingResourceException mre) {
                // Handle BusinessLogicException where the errorCode has not been
                // configured with an HTTP Status Code or Error message
                String message = String.format("The error code %s could not be found in the "
                  + "ErrorHttpCodes.properties or ErrorMessages.properties files", ex.getErrorCode());
                log.severe(message);
                return Response
                  .status(Status.INTERNAL_SERVER_ERROR)
                  .entity(message)
                  .build();
            }
        }
        // Handle BusinessLogicException where either the cause cannot be handled
        // and the errorCode is null. The application server will handle the RuntimeException.
        throw new RuntimeException(ex);
    }

}
