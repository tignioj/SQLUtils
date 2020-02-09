package com.tignioj.sqlutil.connector.qr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface MyQueryRunner {

    <T> List<T> query(String sql, Class<T> c) throws SQLException;

    <T> List<T> query(String sql, Class<T> c, boolean hasChildren) throws SQLException;

    <T> List<T> query(String sql, Object[] params, Class<T> c, boolean hasChildren) throws SQLException;

    /**
     * 分页查询
     * @param sql 查询语句
     * @param params 参数
     * @param c
     * @param hasChildren
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> queryPage(String sql, Object[] params, Class<T> c, int startIndex, int totoalcount, boolean hasChildren) throws SQLException;

    void releaseConnection(Connection connection);

    PreparedStatement initParams(Connection connection, Object[] params, String sql) throws SQLException;

    boolean insert(String sql, Object[] params) throws SQLException;

    boolean delete(String sql, Object[] params) throws SQLException;

    int update(String sql, Object[] params) throws SQLException;
}
