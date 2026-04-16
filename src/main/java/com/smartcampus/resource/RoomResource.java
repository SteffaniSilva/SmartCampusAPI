/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

/**
 *
 * @author silva
 */
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ApiError;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @Context
    private UriInfo uriInfo;

    @GET
    public Collection<Room> getAllRooms() {
        return DataStore.ROOMS.values();
    }

    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()
                || room.getName() == null || room.getName().isBlank()) {
            throw badRequest("Room id and name are required.");
        }

        if (DataStore.ROOMS.containsKey(room.getId())) {
            throw conflict("Room with id '" + room.getId() + "' already exists.");
        }

        DataStore.ROOMS.put(room.getId(), room);

        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Room getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.ROOMS.get(roomId);

        if (room == null) {
            throw notFound("Room with id '" + roomId + "' was not found.");
        }

        return room;
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.ROOMS.get(roomId);

        if (room == null) {
            throw notFound("Room with id '" + roomId + "' was not found.");
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted because it still has sensors assigned."
            );
        }

        DataStore.ROOMS.remove(roomId);

        return Response.ok(Collections.singletonMap("message", "Room deleted successfully.")).build();
    }

    private WebApplicationException badRequest(String message) {
        ApiError error = new ApiError(400, "Bad Request", message, "/api/v1/rooms");
        return new WebApplicationException(
                Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(error)
                        .build()
        );
    }

    private WebApplicationException notFound(String message) {
        ApiError error = new ApiError(404, "Not Found", message, "/api/v1/rooms");
        return new WebApplicationException(
                Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(error)
                        .build()
        );
    }

    private WebApplicationException conflict(String message) {
        ApiError error = new ApiError(409, "Conflict", message, "/api/v1/rooms");
        return new WebApplicationException(
                Response.status(Response.Status.CONFLICT)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(error)
                        .build()
        );
    }
}
