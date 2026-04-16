/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

/**
 *
 * @author silva
 */
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ApiError;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> getReadings() {
        Sensor sensor = DataStore.SENSORS.get(sensorId);
        if (sensor == null) {
            throw notFound("Sensor with id '" + sensorId + "' was not found.");
        }

        return DataStore.getReadingsForSensor(sensorId);
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.SENSORS.get(sensorId);
        if (sensor == null) {
            throw notFound("Sensor with id '" + sensorId + "' was not found.");
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is in MAINTENANCE and cannot accept new readings."
            );
        }

        if (reading == null) {
            throw badRequest("Reading body is required.");
        }

        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }

        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        DataStore.getReadingsForSensor(sensorId).add(reading);
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED)
                .entity(reading)
                .build();
    }

    private WebApplicationException badRequest(String message) {
        ApiError error = new ApiError(400, "Bad Request", message, "/api/v1/sensors/" + sensorId + "/readings");
        return new WebApplicationException(
                Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(error)
                        .build()
        );
    }

    private WebApplicationException notFound(String message) {
        ApiError error = new ApiError(404, "Not Found", message, "/api/v1/sensors/" + sensorId + "/readings");
        return new WebApplicationException(
                Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(error)
                        .build()
        );
    }
}
