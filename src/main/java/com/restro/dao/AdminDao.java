package com.restro.dao;

import com.restro.dto.AdminDTO;

import java.sql.SQLException;
import java.time.LocalDateTime;

public interface AdminDao {

    AdminDTO findByEmail(String email) throws SQLException;

    AdminDTO findById(int adminId) throws SQLException;

    boolean update(AdminDTO admin) throws SQLException;

    boolean updatePassword(int adminId, String passwordHash, String passwordSalt) throws SQLException;

    boolean updateLastLogin(int adminId, LocalDateTime loginTime) throws SQLException;
}
