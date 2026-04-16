/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

/**
 *
 * @author silva
 */
import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Map<String, Object> getApiInfo() {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("name", "Smart Campus Sensor & Room Management API");
        response.put("version", "v1");

        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("owner", "Smart Campus Steffani");
        contact.put("email", "w2120418@westminster.ac.uk");
        response.put("contact", contact);

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("self", "/api/v1");
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        response.put("resources", resources);

        return response;
    }
}
