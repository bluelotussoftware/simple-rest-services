/*
 * Copyright 2015-2016 Blue Lotus Software, LLC.
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
package com.bluelotussoftware.rest.services;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * This REST API is designed to provide a series of simple services for use in
 * integration testing, or for checking various application requirements such as
 * handling slow connections, or responses including unexpected responses.
 *
 * @author John Yeary <jyeary@bluelotussoftware.com>
 * @version 1.0
 */
@Path("v1")
public class WebResource {

    /**
     * The period of time to sleep in seconds.
     */
    @DefaultValue("0")
    @QueryParam("sleep")
    @Min(value = 0, message = "The value must be greater than or equal to 0.")
    @Max(value = 900, message = "The value must be less than or equal to 900.")
    private int sleep;

    /**
     * The requested HTTP status code to be returned. The default is 200 - OK.
     */
    @DefaultValue("200")
    @QueryParam("response")
    private int response;

    /**
     * Method handling HTTP GET requests. The returned object will be sent to
     * the client as "application/json" media type.
     *
     * @param headers The HTTP Request Headers.
     * @param request
     * @return String that will be returned as a text/plain response.
     */
    @Path("/services")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getResponse(@Context HttpHeaders headers, @Context Request request) {
        return process(request, headers, null);
    }

    @Path("/services/{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPath(@PathParam("id") String id, @Context HttpHeaders headers, @Context Request request) {
        return process(request, headers, id);
    }

    private Response process(Request request, HttpHeaders headers, String id) {
        try {
            Thread.sleep(sleep * 1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(WebResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        Status status = Response.Status.fromStatusCode(response);

        if (status == null) {
            status = Status.BAD_REQUEST;
        }

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(buildJsonObject(request, headers, status, id).toString())
                .build();
    }

    private JsonObject buildJsonObject(Request request, HttpHeaders headers, Status status, String id) {

        JsonObjectBuilder params = Json.createObjectBuilder()
                .add("sleep", sleep)
                .add("response", response);

        if (id == null) {
            params.addNull("id");
        } else {
            params.add("id", id);
        }

        return Json.createObjectBuilder()
                .add("usage", Json.createObjectBuilder()
                        .add("sleep", "The number of seconds to sleep passed as a query parameter e.g. ?sleep=10. The default is 0.")
                        .add("response", "The requested response status passed as a query parameter e.g. ?response=404. "
                                + "The default is 200 - OK. "
                                + "If the value is not valid, a 400 - Bad Request is returned."))
                .add("request", Json.createObjectBuilder()
                        .add("method", request.getMethod())
                        .add("accept", headers.getAcceptableMediaTypes().toString())
                        .add("user-agent", headers.getHeaderString("user-agent"))
                        .add("params", params))
                .add("status", Json.createObjectBuilder()
                        .add("status code", status.getStatusCode())
                        .add("reason", status.getReasonPhrase())
                        .add("family", status.getFamily().name()))
                .add("info", Json.createObjectBuilder()
                        .add("author", "John Yeary")
                        .add("twitter", "@jyeary")
                        .add("blog", "http://javaevangelist.blogspot.com")
                        .add("version", "1.0.0")
                )
                .build();
    }
}
