package com.franroa.liquibase.resources;


import com.franroa.liquibase.dto.MyModelResponse;
import com.franroa.liquibase.models.MyModel;
import org.javalite.activejdbc.Base;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MyResource {
    @POST
    @PermitAll
    public Response store() {
        Base.open("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/testdb", "postgres", "password");

        MyModel myModel = new MyModel();
        myModel.set("name", "That is hardcoded a name").saveIt();

        Base.close();

        return Response.ok().build();
    }

    @GET
    @PermitAll
    @Path("{id}")
    public Response fetch(@PathParam("id") Integer id) {
        Base.open("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/testdb", "postgres", "password");

        String myModelName = MyModel.findById(id).get("name").toString();

        Base.close();

        return Response.ok().entity(new MyModelResponse(myModelName)).build();
    }
}
