/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.store;

/**
 *
 * @author silva
 */
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    public static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> SENSORS = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> READINGS = new ConcurrentHashMap<>();

    static {
        Room room1 = new Room("LIB-301", "Library Quiet Study", 80);
        ROOMS.put(room1.getId(), room1);
    }

    private DataStore() {
    }

    public static List<SensorReading> getReadingsForSensor(String sensorId) {
        if (!READINGS.containsKey(sensorId)) {
            READINGS.put(sensorId, new ArrayList<>());
        }
        return READINGS.get(sensorId);
    }
}
