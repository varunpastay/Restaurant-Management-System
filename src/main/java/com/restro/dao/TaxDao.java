package com.restro.dao;

import com.restro.dto.TaxDTO;

import java.sql.SQLException;
import java.util.List;

public interface TaxDao {

    List<TaxDTO> findAllByRestaurant(int restaurantId) throws SQLException;

    List<TaxDTO> findActiveByRestaurant(int restaurantId) throws SQLException;

    TaxDTO findById(int taxId) throws SQLException;

    int insert(TaxDTO tax) throws SQLException;

    boolean update(TaxDTO tax) throws SQLException;

    boolean delete(int taxId) throws SQLException;
}
