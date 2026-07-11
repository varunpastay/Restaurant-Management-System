package com.restro.dao;

import com.restro.dto.UploadedFileDTO;

import java.sql.SQLException;

public interface UploadedFileDao {

    /** Saves a new file and returns the generated file_id. relativePath must not already exist. */
    int insert(String relativePath, String contentType, byte[] data) throws SQLException;

    UploadedFileDTO findByPath(String relativePath) throws SQLException;

    boolean deleteByPath(String relativePath) throws SQLException;
}
