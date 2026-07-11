package com.restro.dao;

import com.restro.dto.QRCodeDTO;

import java.sql.SQLException;

public interface QRCodeDao {

    int insert(QRCodeDTO qrCode) throws SQLException;

    /** Most recently generated QR for a table (a table can be regenerated, keeping prior rows for audit). */
    QRCodeDTO findLatestByTable(int tableId) throws SQLException;
}
