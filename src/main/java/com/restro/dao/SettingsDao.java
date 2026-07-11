package com.restro.dao;

import com.restro.dto.SettingsDTO;

import java.sql.SQLException;
import java.util.List;

public interface SettingsDao {

    List<SettingsDTO> findAllByRestaurant(int restaurantId) throws SQLException;

    /** Returns null if the key has never been set for this restaurant. */
    String findValue(int restaurantId, String key) throws SQLException;

    /** Inserts the key if absent, otherwise updates its value. */
    void upsert(int restaurantId, String key, String value) throws SQLException;
}
