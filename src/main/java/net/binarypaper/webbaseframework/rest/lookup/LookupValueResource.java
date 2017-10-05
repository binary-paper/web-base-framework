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

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import lombok.extern.java.Log;
import net.binarypaper.webbaseframework.entity.AuditRevision;
import net.binarypaper.webbaseframework.entity.DatedEntity;
import net.binarypaper.webbaseframework.entity.lookup.LookupValue;
import net.binarypaper.webbaseframework.rest.AuditRevisionHelper;
import net.binarypaper.webbaseframework.rest.BusinessLogicException;
import net.binarypaper.webbaseframework.rest.PersistenceHelper;
import net.binarypaper.webbaseframework.rest.ResponseError;
import net.binarypaper.webbaseframework.rest.SwaggerBootstrap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Lookup Value REST Web Service
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// EJB annotations
@Stateless
// JAX-RS annotations
@Path("lookup_values")
@Consumes(MediaType.APPLICATION_JSON)
@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
// Security annotations
@RolesAllowed("view_lookup_values")
// Swagger annotations
@Api(value = "Lookup Values", authorizations = {
    @Authorization(value = SwaggerBootstrap.O_AUTH_2)
})
// Lombok annotations
@Log
public class LookupValueResource {

    @PersistenceContext(unitName = "WebBaseFrameworkPU")
    private EntityManager em;

    @Context
    private UriInfo uriInfo;

    @Resource
    private SessionContext sessionContext;

    // JAX-RS annotations
    @POST
    // Security annotations
    @RolesAllowed("manage_lookup_values")
    // Jackson annotations
    @JsonView(LookupValue.View.All.class)
    // Swagger annotations
    @ApiOperation(value = "Add a lookup value",
            notes = "Add a lookup value",
            code = 201,
            response = LookupValue.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "The input data is invalid", response = ResponseError.class)
        ,
        @ApiResponse(code = 403, message = "Not authorized to call the api")
    })
    public Response addLookupValue(
            @QueryParam(value = "parent_id")
            @ApiParam(value = "The id of the parent lookup value") Long parentId,
            @JsonView(LookupValue.View.Add.class) LookupValue lookupValue) throws BusinessLogicException {
        if (parentId != null) {
            if (!parentId.equals(lookupValue.getParentId())) {
                throw new BusinessLogicException("0001", Status.BAD_REQUEST.getStatusCode());
            }
            LookupValue parent = em.find(LookupValue.class, parentId);
            if (parent == null) {
                throw new BusinessLogicException("0002", Status.BAD_REQUEST.getStatusCode());
            }
            if (lookupValue.getLookupListName().equals(parent.getLookupListName())) {
                throw new BusinessLogicException("0003", Status.BAD_REQUEST.getStatusCode());
            }
            lookupValue.setParent(parent);
            parent.getChildren().add(lookupValue);
        } else {
            // If the parent id in the query string is null,
            // but a parent id is specified in the lookupList
            if (lookupValue.getParentId() != null) {
                throw new BusinessLogicException("0001", Status.BAD_REQUEST.getStatusCode());
            }
        }
        PersistenceHelper<LookupValue> persistenceHelper = new PersistenceHelper<>(LookupValue.class, em, sessionContext.getCallerPrincipal());
        persistenceHelper.addConstraintValidation("UC_LOOKUP_LIST_VALUE", "0004");
        lookupValue = persistenceHelper.persistEntity(lookupValue);
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path(lookupValue.getId().toString());
        return Response.created(uriBuilder.build()).entity(lookupValue).build();
    }

    // JAX-RS annotations
    @Path("lookup_list_name/{lookup_list_name}")
    @GET
    // Jackson annotations
    @JsonView(LookupValue.View.List.class)
    // Swagger annotations
    @ApiOperation(value = "Get all lookup values for the lookup list name",
            notes = "Get all lookup values for the lookup list name",
            code = 200,
            responseContainer = "List",
            response = LookupValue.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No lookup values to return")
        ,
        @ApiResponse(code = 400, message = "The input data is invalid", response = ResponseError.class)
        ,
        @ApiResponse(code = 403, message = "Not authorized to call the api")
    })
    public Response getLookupValuesByLookupListName(
            @PathParam("lookup_list_name")
            @ApiParam(value = "The name of the lookup list", required = true)
            final String lookupListName,
            @QueryParam("parent_id")
            @ApiParam(value = "The id of the parent lookup value")
            final Long parentId,
            @QueryParam("active")
            @ApiParam(value = "The active status of the lookup value")
            final Boolean active,
            @QueryParam("effective_date")
            @ApiParam(value = "The effective date in the format yyyy-MM-dd by which lookup values will be filtered")
            final String effectiveDateString) throws BusinessLogicException {
        TypedQuery<LookupValue> query;
        if (parentId == null) {
            query = em.createNamedQuery("LookupValue.findByLookupListName", LookupValue.class);
        } else {
            query = em.createNamedQuery("LookupValue.findByLookupListNameAndParentId", LookupValue.class);
            query.setParameter("parentId", parentId);
        }
        query.setParameter("lookupListName", lookupListName);
        List<LookupValue> lookupValues = query.getResultList();
        lookupValues = LookupValue.filterByActiveStatus(lookupValues, active);
        if (effectiveDateString != null) {
            Date effectiveDate = DatedEntity.parseDate(effectiveDateString, "0007");
            lookupValues = LookupValue.filterByEffectiveDate(lookupValues, effectiveDate);
        }
        if (lookupValues.isEmpty()) {
            return Response.status(Status.NO_CONTENT).build();
        }
        return Response.ok(lookupValues).build();
    }

    // JAX-RS annotations
    @Path("{lookup_value_id}")
    @GET
    // Jackson annotations
    @JsonView(LookupValue.View.All.class)
    // Swagger annotations
    @ApiOperation(value = "Get a lookup value by id",
            notes = "Get a lookup value by id",
            code = 200,
            response = LookupValue.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = "Not authorized to call the api")
        ,
        @ApiResponse(code = 404, message = "No lookup value to return")
    })
    public Response getLookupValueById(
            @PathParam("lookup_value_id")
            @ApiParam(value = "The id of the lookup value", required = true)
            final Long lookupValueId) throws BusinessLogicException {
        LookupValue lookupValue = em.find(LookupValue.class, lookupValueId);
        if (lookupValue == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(lookupValue).build();
    }

    // JAX-RS annotations
    @Path("{lookup_value_id}")
    @PUT
    // Security annotations
    @RolesAllowed("manage_lookup_values")
    // Jackson annotations
    @JsonView({LookupValue.View.All.class})
    // Swagger annotations
    @ApiOperation(value = "Update lookup value",
            notes = "Update lookup value",
            code = 202,
            response = LookupValue.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "The input data is invalid", response = ResponseError.class)
        ,
        @ApiResponse(code = 403, message = "Not authorized to call the api")
    })
    public Response updateLookupValue(
            @PathParam("lookup_value_id")
            @ApiParam(value = "The id of the lookup value")
            final Long lookupValueId,
            @JsonView(LookupValue.View.Edit.class) LookupValue lookupValue) throws BusinessLogicException {
        if (!lookupValueId.equals(lookupValue.getId())) {
            throw new BusinessLogicException("0005", Status.BAD_REQUEST.getStatusCode());
        }
        LookupValue fromDB = em.find(LookupValue.class, lookupValue.getId());
        if (fromDB == null) {
            throw new BusinessLogicException("0006", Status.BAD_REQUEST.getStatusCode());
        }
        PersistenceHelper<LookupValue> persistenceHelper = new PersistenceHelper<>(LookupValue.class, em, sessionContext.getCallerPrincipal());
        persistenceHelper.addConstraintValidation("UC_LOOKUP_LIST_VALUE", "0004");
        fromDB = persistenceHelper.updateEntity(fromDB, lookupValue);
        return Response.accepted(fromDB).build();
    }

    // JAX-RS annotations
    @Path("{lookup_value_id}")
    @DELETE
    // Security annotations
    @RolesAllowed("delete_lookup_values")
    // Swagger annotations
    @ApiOperation(value = "Delete a lookup value by id",
            notes = "Delete a lookup value by id",
            code = 200,
            response = String.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = "Not authorized to call the api")
        ,
        @ApiResponse(code = 400, message = "The lookup value could not be deleted", response = ResponseError.class)
        ,
        @ApiResponse(code = 404, message = "No lookup value to remove", response = ResponseError.class)
    })
    public Response deleteLookupValueById(
            @PathParam("lookup_value_id")
            @ApiParam(value = "The id of the lookup value")
            final Long lookupValueId) throws BusinessLogicException {
        LookupValue lookupValue = em.find(LookupValue.class, lookupValueId);
        if (lookupValue == null) {
            throw new BusinessLogicException("0006", Status.NOT_FOUND.getStatusCode());
        }
        if (lookupValue.getParent() != null) {
            lookupValue.getParent().removeChild(lookupValue);
        }
        PersistenceHelper<LookupValue> persistenceHelper = new PersistenceHelper<>(LookupValue.class, em, sessionContext.getCallerPrincipal());
        persistenceHelper.addConstraintValidation("FK_LOOKUP_VALUE_PARENT", "0008");
        persistenceHelper.deleteEntity(lookupValue);
        return Response.ok().build();
    }

    // JAX-RS annotations
    @Path("{lookup_value_id}/revisions")
    @GET
    // Security annotations
    @RolesAllowed("view_audit_revisions")
    // Jackson annotations
    @JsonView(AuditRevision.class)
    // Swagger annotations
    @ApiOperation(value = "Get a list of audit database revisions for a given lookup value id",
            notes = "Get a list of audit database revisions for a given lookup value id",
            code = 200,
            responseContainer = "List",
            response = LookupValue.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No lookup value revisions to return")
        ,
        @ApiResponse(code = 403, message = "Not authorized to call the api")
    })
    public Response getLookupValueRevisions(
            @PathParam("lookup_value_id")
            @ApiParam(value = "The id of the lookup value")
            final Long lookupValueId) throws BusinessLogicException {
        AuditRevisionHelper auditRevisionHelper = new AuditRevisionHelper(LookupValue.class);
        List<LookupValue> revisions = auditRevisionHelper.getAllAuditRevisions(em, lookupValueId);
        if (revisions.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(revisions).build();
    }

    // JAX-RS annotations
    @Path("csv_upload")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    // Security annotations
    @RolesAllowed("manage_lookup_values")
    // Jackson annotations
    @JsonView(LookupValue.View.List.class)
    // Swagger annotations
    @ApiOperation(value = "Upload a CSV file containing lookup values to be added",
            notes = "Upload a CSV file containing lookup values to be added. "
            + "The header record of the CSV file should be: "
            + "LOOKUP_LIST_NAME, DISPLAY_VALUE, ACTIVE, EFFECTIVE_FROM, EFFECTIVE_TO, PARENT_LOOKUP_LIST_NAME, PARENT_DISPLAY_VALUE",
            code = 200,
            responseContainer = "List",
            response = LookupValue.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "The uploaded file is invalid", response = ResponseError.class)
        ,
        @ApiResponse(code = 403, message = "Not authorized to call the api")
    })
    @ApiImplicitParams(
            @ApiImplicitParam(dataType = "file", paramType = "form", name = "csvFile")
    )
    public Response uploadCsvFile(@ApiParam(hidden = true) MultipartFormDataInput input) throws BusinessLogicException {
        List<LookupValue> lookupValues = new ArrayList<>();
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("csvFile");
        if (inputParts == null) {
            throw new BusinessLogicException("0009", Status.BAD_REQUEST.getStatusCode());
        }
        for (InputPart inputPart : inputParts) {
            try {
                // Convert the uploaded file to inputstream
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                // Read the input stream with Apache Commons CSV
                Reader reader = new InputStreamReader(inputStream);
                Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(reader);
                for (CSVRecord record : records) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    LookupValue lookupValue = new LookupValue();
                    lookupValue.setLookupListName(record.get("LOOKUP_LIST_NAME").trim());
                    lookupValue.setDisplayValue(record.get("DISPLAY_VALUE").trim());
                    lookupValue.setActive(Boolean.parseBoolean(record.get("ACTIVE").trim()));
                    if (!record.get("EFFECTIVE_FROM").trim().isEmpty()) {
                        Date effectiveFromDate = dateFormat.parse(record.get("EFFECTIVE_FROM").trim());
                        lookupValue.setEffectiveFrom(effectiveFromDate);
                    }
                    if (!record.get("EFFECTIVE_TO").trim().isEmpty()) {
                        Date effectiveToDate = dateFormat.parse(record.get("EFFECTIVE_TO").trim());
                        lookupValue.setEffectiveTo(effectiveToDate);
                    }
                    if (!record.get("PARENT_LOOKUP_LIST_NAME").trim().isEmpty()
                            && !record.get("PARENT_DISPLAY_VALUE").trim().isEmpty()) {
                        TypedQuery<LookupValue> query = em.createNamedQuery("LookupValue.findByLookupListNameAndDisplayValue", LookupValue.class);
                        query.setParameter("lookupListName", record.get("PARENT_LOOKUP_LIST_NAME").trim());
                        query.setParameter("displayValue", record.get("PARENT_DISPLAY_VALUE").trim());
                        LookupValue parent = query.getSingleResult();
                        if (lookupValue.getLookupListName().equals(parent.getLookupListName())) {
                            throw new BusinessLogicException("0010", Status.BAD_REQUEST.getStatusCode());
                        }
                        parent.addChild(lookupValue);
                    }
                    System.out.println(lookupValue);
                    PersistenceHelper<LookupValue> persistenceHelper = new PersistenceHelper<>(LookupValue.class, em, sessionContext.getCallerPrincipal());
                    persistenceHelper.addConstraintValidation("UC_LOOKUP_LIST_VALUE", "0011");
                    lookupValue = persistenceHelper.persistEntity(lookupValue);
                    lookupValues.add(lookupValue);
                }
            } catch (IOException ex) {
                throw new BusinessLogicException("0012", Status.BAD_REQUEST.getStatusCode());
            } catch (ParseException ex) {
                throw new BusinessLogicException("0013", Status.BAD_REQUEST.getStatusCode());
            } catch (NoResultException ex) {
                throw new BusinessLogicException("0014", Status.BAD_REQUEST.getStatusCode());
            } catch (NonUniqueResultException ex) {
                throw new BusinessLogicException("0015", Status.BAD_REQUEST.getStatusCode());
            } catch (IllegalArgumentException ex) {
                throw new BusinessLogicException("0016", Status.BAD_REQUEST.getStatusCode());
            }
        }
        return Response.ok(lookupValues).build();
    }

}
