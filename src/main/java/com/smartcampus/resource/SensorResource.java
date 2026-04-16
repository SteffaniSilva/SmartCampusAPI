/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

/**
 *
 * @author silva
 */
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ApiError;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @Context
    private UriInfo uriInfo;

    @GET
    public Collection<Sensor> getAllSensors(@QueryParam("type") String type) {
        if (type == null || type.isBlank()) {
            return DataStore.SENSORS.values();
        }

        List<Sensor> filtered = new ArrayList<>();
        for (Sensor sensor : DataStore.SENSORS.values()) {
            if (sensor.getType() != null && sensor.getType().equalsIgnoreCase(type)) {
                filtered.add(sensor);
            }
        }
        return filtered;
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()
                || sensor.getType() == null || sensor.getType().isBlank()
                || sensor.getStatus() == null || sensor.getStatus().isBlank()
                || sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            throw badRequest("Sensor id, type, status and roomId are required.");
        }

        if (DataStore.SENSORS.containsKey(sensor.getId())) {
            throw conflict("Sensor with id '" + sensor.getId() + "' already exists.");
        }

        Room room = DataStore.ROOMS.get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "Cannot create sensor because roomId '" + sensor.getRoomId() + "' does not exist."
            );
        }

        DataStore.SENSORS.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Sensor getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.SENSORS.get(sensorId);

        if (sensor == null) {
            throw notFound("Sensor with id '" + sensorId + "' was not found.");
        }

        return sensor;
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.SENSORS.get(sensorId);

        if (sensor == null) {
            throw notFound("Sensor with id '" + sensorId + "' was not found.");
        }

        return new SensorReadingResource(sensorId);
    }

    private WebApplicationException badRequest(String message) {
        ApiError error = new ApiError(400, "Bad Request", message, "/api/v1/sensors");
        return new WebApplicationException(
                Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(error)
                        .build()
        );
    }

    private WebApplicationException notFound(String message) {
        ApiError error = new ApiError(404, "Not Found", message, "/api/v1/sensors");
        return new WebApplicationException(
                Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(error)
                        .build()
        );
    }

    private WebApplicationException conflict(String message) {
        ApiError error = new ApiError(409, "Conflict", message, "/api/v1/sensors");
        return new WebApplicationException(
                Response.status(Response.Status.CONFLICT)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(error)
                        .build()
        );
    }
}
