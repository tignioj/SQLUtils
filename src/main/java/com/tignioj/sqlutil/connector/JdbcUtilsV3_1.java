package com.tignioj.sqlutil.connector;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 3.1更新：处理多线程并发访问问题：
 * 当多个线程调用事务方法时，可能会出现操作同一个connection的方法
 * 因此需要为每一个线程分配一个Connection
 * 解决：ThreadLocal<Connection>
 */
public class JdbcUtilsV3_1 {
    //只用文件的默认配置, 要求你必须给出c3p0-config.xml!
    private static ComboPooledDataSource dataSource = new ComboPooledDataSource();


    /**
     * 它是事务专用连接
     * 实现原理：
     * 1. 调用开启事务的方法，才会给con赋值, 不调用开启事务方法，则获取的连接是非事务连接
     * 2. 提交/回滚事务时，必须设置为null, 以便于下次获取事务连接是新的事务连接
     * 3. 当服务处要关闭连接时，需要判断是否为事务连接，如果不是则将其传递过来的连接关闭!
     */
//    private static Connection con = null;
    private static ThreadLocal<Connection> t1 = new ThreadLocal<>();

    public static Connection getConnection() throws SQLException {
        //先获取自己线程是否已经存在Connection
        Connection con = t1.get();
        //当con不等于null 说明已经调用过beginTransaction()
        if (con != null) {
            return con;
        }

        //直接从连接池里面拿
        return dataSource.getConnection();
    }

    /**
     * 返回连接池对象
     * @return
     */
    public static ComboPooledDataSource getDataSources() {
        return dataSource;
    }

    /**
     * 开启事务
     * 1. 获取一个Connection, 设置它的setAutoCommit(false)
     * 2. 还要保证dao中使用的链接是我们刚刚创建的
     * ---------------需求-------
     * 1. 创建一个Connection, 设置为手动提交
     * 2. 把这个Connection给dao用
     * 3. 还要让commitTransaction或rollbackTransaction可以获取到！
     *
     */
    public static void beginTransaction() throws SQLException {
        Connection con = t1.get();
        if (con != null) {
            throw new SQLException("已经开启了事务，就不要重复开启了!");
        }
        /**
         * 1. 给con赋值！
         * 2. 给con设置为手动提交！
         */
        con = getConnection(); //给con赋值，表示事务已经开启了
        con.setAutoCommit(false);
        t1.set(con); //把当前的线程连接保存起来！
    }

    /**
     * 提交事务
     * 1. 获取beginTransaction提供的Connection， 然后调用commit方法
     */
    public static void commitTransaction() throws SQLException {
        Connection con = t1.get(); //获取当前线程的专用连接
        if (con == null) {
            throw new SQLException("还没有开启事务，不能回滚！");
        }
        /**
         * 1. 直接使用commit()
         */
        con.commit();
        con.close();
        //如果不为null, 返回给连接池时，下次再获取con可能还是上次的连接，因此需要将其设置为null
        //将其设置为null, 表示事务已经结束了！下次再去调用getConnection()返回就不会是原来的con了
//        con = null;
        t1.remove();//从t1中移除连接
    }

    /**
     * 回滚事务
     * 1. 获取beginTransaction提供的Connection , 然后调用rollback方法
     */
    public static void rollbackTransaction() throws SQLException {
        Connection con = t1.get();
        if (con == null) {
            throw new SQLException("还没有开启事务，不能回滚！");
        }
        /**
         * 1. 直接使用rollback()
         */
        con.rollback();
        con.close();
//        con = null;
        t1.remove();
    }

    /**
     * 释放连接
     */
    public static void releaseConnection(Connection connection) throws SQLException {
        Connection con = t1.get();
        /**
         * 判断它是不是事务专用，如果是就不关闭！
         * 如果不是事务专用，那么就要关闭！
         */

        /**
         * 如果con == null，说明现在没有事务，
         * 那么connection一定不是事务专用的！
         */
        if (con == null) {
            connection.close();
        }

        /**
         * 如果con != null 说明有事务，那么需要判断参数连接是否与con相等，
         * 如果不等, 说明参数连接不是事务专用
         */
        if(con != connection) {
            connection.close();
        }
    }
}