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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class used to marshal an error as a JSON object for a REST response
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Lombok annotations
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseError {

    /**
     * The unique error code of the error
     *
     * @return The error code
     */
    // Jackson annotations
    @JsonProperty(value = "code")
    // Swagger annotations
    @ApiModelProperty(value = "The error code", example = "0001", readOnly = true)
    private String errorCode;

    /**
     * The message of the error
     *
     * @return The error message
     */
    // Swagger annotations
    @ApiModelProperty(value = "The error message", example = "The X is required", readOnly = true)
    private String message;

}
