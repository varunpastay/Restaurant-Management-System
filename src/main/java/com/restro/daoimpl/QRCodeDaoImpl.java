package com.restro.daoimpl;

import com.restro.dao.QRCodeDao;
import com.restro.dto.QRCodeDTO;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QRCodeDaoImpl implements QRCodeDao {

    @Override
    public int insert(QRCodeDTO qrCode) throws SQLException {
        String sql = "INSERT INTO qr_code (table_id, image_path, target_url) VALUES (?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, qrCode.getTableId());
            ps.setString(2, qrCode.getImagePath());
            ps.setString(3, qrCode.getTargetUrl());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public QRCodeDTO findLatestByTable(int tableId) throws SQLException {
        String sql = "SELECT qr_code_id, table_id, image_path, target_url, generated_at FROM qr_code " +
                "WHERE table_id = ? ORDER BY generated_at DESC LIMIT 1";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                QRCodeDTO q = new QRCodeDTO();
                q.setQrCodeId(rs.getInt("qr_code_id"));
                q.setTableId(rs.getInt("table_id"));
                q.setImagePath(rs.getString("image_path"));
                q.setTargetUrl(rs.getString("target_url"));
                q.setGeneratedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("generated_at")));
                return q;
            }
        }
    }
}
