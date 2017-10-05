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

import javax.ejb.ApplicationException;
import lombok.Getter;

/**
 * A generic Business Logic exception intended to be used by the business layer
 * of the application.
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// EJB annotations
@ApplicationException(rollback = true)
public class BusinessLogicException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * The unique error code of the error
     *
     * @return The error code
     */
    // Lombok annotations
    @Getter
    private final String errorCode;

    /**
     * The HTTP status code of the error
     *
     * @return The HTTP status code
     */
    // Lombok annotations
    @Getter
    private final int httpStatusCode;

    /**
     * Constructs an instance of <code>BusinessLogicException</code> with the
     * specified error code.
     *
     * @param errorCode the unique error code of the error
     * @param httpStatusCode the HTTP status code of the error
     */
    public BusinessLogicException(String errorCode, int httpStatusCode) {
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Constructs an instance of <code>BusinessLogicException</code> with the
     * specified error code.
     *
     * @param errorCode the unique error code of the error
     * @param message the additional error message of the error code
     * @param httpStatusCode the HTTP status code of the error
     */
    public BusinessLogicException(String errorCode, String message, int httpStatusCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Constructs an instance of <code>BusinessLogicException</code> with the
     * specified cause.
     * <p>
     * The HTTP status code will be set to 400 - Bad Request
     *
     * @param cause The cause of the exception
     */
    public BusinessLogicException(Throwable cause) {
        super(cause);
        errorCode = null;
        this.httpStatusCode = 400;
    }

}
