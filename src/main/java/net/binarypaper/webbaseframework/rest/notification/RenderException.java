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

import javax.ejb.ApplicationException;

/**
 * Exception thrown when rendering issues occur
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// EJB annotations
@ApplicationException(rollback = true)
public class RenderException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>RenderException</code> without detail
     * message.
     */
    public RenderException() {
    }

    /**
     * Constructs an instance of <code>RenderException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public RenderException(String msg) {
        super(msg);
    }
}
