package com.restro.dao;

import com.restro.dto.StaffDTO;

import java.sql.SQLException;
import java.util.List;

public interface StaffDao {

    List<StaffDTO> findAllByRestaurant(int restaurantId) throws SQLException;

    StaffDTO findById(int staffId) throws SQLException;

    StaffDTO findByEmail(String email) throws SQLException;

    int insert(StaffDTO staff) throws SQLException;

    boolean update(StaffDTO staff) throws SQLException;

    boolean delete(int staffId) throws SQLException;

    boolean updatePassword(int staffId, String passwordHash, String passwordSalt) throws SQLException;
}
