package com.restro.daoimpl;

import com.restro.dao.UploadedFileDao;
import com.restro.dto.UploadedFileDTO;
import com.restro.utility.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UploadedFileDaoImpl implements UploadedFileDao {

    private static final String SELECT_BASE =
            "SELECT file_id, relative_path, content_type, data, created_at FROM uploaded_file ";

    @Override
    public int insert(String relativePath, String contentType, byte[] data) throws SQLException {
        String sql = "INSERT INTO uploaded_file (relative_path, content_type, data) VALUES (?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, relativePath);
            ps.setString(2, contentType);
            ps.setBytes(3, data);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public UploadedFileDTO findByPath(String relativePath) throws SQLException {
        String sql = SELECT_BASE + "WHERE relative_path = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, relativePath);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                UploadedFileDTO f = new UploadedFileDTO();
                f.setFileId(rs.getInt("file_id"));
                f.setRelativePath(rs.getString("relative_path"));
                f.setContentType(rs.getString("content_type"));
                f.setData(rs.getBytes("data"));
                f.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
                return f;
            }
        }
    }

    @Override
    public boolean deleteByPath(String relativePath) throws SQLException {
        String sql = "DELETE FROM uploaded_file WHERE relative_path = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, relativePath);
            return ps.executeUpdate() > 0;
        }
    }
}
