package com.restro.daoimpl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Null-safe JDBC read/write helpers shared by every DAO implementation in
 * this package. {@code PreparedStatement.setObject(idx, null)} is ambiguous
 * about the target SQL type on some drivers, so nullable date/time
 * parameters go through {@code setNull} explicitly instead.
 */
final class JdbcUtil {

    private JdbcUtil() {
    }

    static void setNullableDate(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value != null) {
            ps.setObject(index, value);
        } else {
            ps.setNull(index, Types.DATE);
        }
    }

    static void setNullableTime(PreparedStatement ps, int index, LocalTime value) throws SQLException {
        if (value != null) {
            ps.setObject(index, value);
        } else {
            ps.setNull(index, Types.TIME);
        }
    }

    static void setNullableTimestamp(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        if (value != null) {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        } else {
            ps.setNull(index, Types.TIMESTAMP);
        }
    }

    static void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    static LocalDate toLocalDate(Date d) {
        return d != null ? d.toLocalDate() : null;
    }
}
