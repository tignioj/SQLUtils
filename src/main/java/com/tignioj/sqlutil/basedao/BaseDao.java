package com.tignioj.sqlutil.basedao;

import java.sql.SQLException;
import java.util.List;

public interface BaseDao<T> {
    /**
     * 查询所有
     * @return
     * @throws SQLException
     */
    List<T> getAll() throws SQLException;

    /**
     * 查询所有
     * @param hasChildren 是否查询外键
     * @return
     * @throws SQLException
     */
    List<T> getAll(boolean hasChildren) throws SQLException;

    /**
     * 条件查询 + 分页查询
     * @param t 查询的对象
     * @param currentPageNumber 当前页码
     * @param itemCountPerPage 每页显示的数量
     * @param hasChildren 是否查询外键
     * @return
     * @throws SQLException
     */
    List<T> getAllLimit(T t, int currentPageNumber, int itemCountPerPage,boolean hasChildren) throws SQLException;

    /**
     * 分页查询
     * @param t 查询的条件
     * @param currentPageNumber 当前页码
     * @param itemCountPerPage 每页显示的数量
     * @return
     * @throws SQLException
     */
    List<T> getAllLimit(T t, int currentPageNumber, int itemCountPerPage) throws SQLException;

    /**
     * 分页查询
     * @param currentPageNumber 当前页码
     * @param itemCountPerPage 每页显示的数量
     * @return
     * @throws SQLException
     */
    List<T> getAllLimit(int currentPageNumber, int itemCountPerPage) throws SQLException;


    /**
     * 条件查询
     * @param t
     * @param hasChildren
     * @param isStrict 将'like'替换为'='
     * @return
     * @throws SQLException
     */
    List<T> getByCondition(T t, boolean hasChildren, boolean isStrict) throws SQLException;

    /**
     * 条件查询
     * @param t 查询的条件
     * @param hasChildren 是否查询外键
     * @return
     * @throws SQLException
     */
    List<T> getByCondition(T t, boolean hasChildren) throws SQLException;


    List<T> getByCondition(T t) throws SQLException;

    /**
     * 添加一条数据到数据库
     * @param t
     * @throws SQLException
     */
    void addOne(T t) throws SQLException;

    /**
     * 删除一条数据到数据库
     * 对象必须指定主键！
     * @param t
     * @return
     * @throws SQLException
     */
    T delete(T t) throws SQLException;

    /**
     * 更新一条数据到数据库
     * 对象必须指定主键！
     * @param t
     * @return
     * @throws SQLException
     */
    T update(T t) throws SQLException;
}
