package com.tignioj.sqlutil.connector.qr;

import com.tignioj.sqlutil.connector.JdbcUtilsV3_1;
import com.tignioj.sqlutil.wrapper.BeanWrapper;

import java.security.PublicKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MyQueryRunnerImpl implements MyQueryRunner {


    public MyQueryRunnerImpl() {
    }

    public Connection getConnection() {
        try {
            return JdbcUtilsV3_1.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public <T> List<T> query(String sql, Class<T> c) throws SQLException {
        return query(sql, null, c, false);
    }

    @Override
    public <T> List<T> query(String sql, Class<T> c , boolean hasChildren) throws SQLException {
        return query(sql, null, c, hasChildren);
    }

    @Override
    public void releaseConnection(Connection connection) {
        try {
            JdbcUtilsV3_1.releaseConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化参数并返回PreparedStatement
     *
     * @param params
     * @param sql
     * @return
     * @throws SQLException
     */
    @Override
    public PreparedStatement initParams(Connection connection, Object[] params, String sql) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (params != null)
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
        return preparedStatement;
    }

    @Override
    public <T> List<T> query(String sql, Object[] params, Class<T> c, boolean hasChildren) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = initParams(connection, params, sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<T> ts = BeanWrapper.<T>wrapAll(resultSet, c, connection, hasChildren);
        releaseConnection(connection);
        return ts;
    }

    @Override
    public <T> List<T> queryPage(String sql, Object[] params, Class<T> c, int startIndex, int totoalcount, boolean hasChildren) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = initParams(connection, params, sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<T> ts = BeanWrapper.<T>wrapAll(resultSet, c, connection, hasChildren);
        releaseConnection(connection);
        return ts;
    }

    public boolean insert(String sql, Object[] params) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = initParams(connection, params, sql);
        boolean execute = preparedStatement.execute();
        releaseConnection(connection);
        return execute;
    }

    @Override
    public boolean delete(String sql, Object[] params) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = initParams(connection, params, sql);
        boolean execute = preparedStatement.execute();
        releaseConnection(connection);
        return execute;
    }

    @Override
    public int update(String sql, Object[] params) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = initParams(connection, params, sql);
        int i = preparedStatement.executeUpdate();
        releaseConnection(connection);
        return i;
    }
}
