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
package net.binarypaper.webbaseframework.entity;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import lombok.Data;
import net.binarypaper.webbaseframework.rest.BusinessLogicException;
import org.hibernate.envers.Audited;

/**
 * Abstract class that may be extended to make an entity class active or
 * inactive
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// JPA annotations
@MappedSuperclass
// Envers annotations
@Audited
// Lombok annotations
@Data
public abstract class ActivatableEntity implements Serializable {

    private static final long serialVersionUID = -9222051304404254188L;

    // JPA annotations
    @Column(name = "ACTIVE")
    // Framework annotations
    @Updatable
    // Bean Validation annotations
    @NotNull(message = "{ActivatableEntity.active.NotNull}")
    // Swagger annotations
    @ApiModelProperty(
      value = "The active status of the entity",
      example = "true"
    )
    private Boolean active;

    public static List filterByActiveStatus(List inputList, Boolean active) throws BusinessLogicException {
        List<ActivatableEntity> activatableEntities = (List<ActivatableEntity>) inputList;
        if (active == null) {
            return inputList;
        }
        List<ActivatableEntity> outputList = new ArrayList<>();
        for (ActivatableEntity activatableEntity : activatableEntities) {
            if (activatableEntity.getActive().equals(active)) {
                outputList.add(activatableEntity);
            }
        }
        return outputList;
    }
}
