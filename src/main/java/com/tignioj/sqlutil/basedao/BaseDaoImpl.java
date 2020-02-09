package com.tignioj.sqlutil.basedao;

import com.tignioj.sqlutil.connector.qr.MyQueryRunnerImpl;
import com.tignioj.sqlutil.wrapper.GenericUtils;
import com.tignioj.sqlutil.wrapper.anno.ColumnInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.*;

import static com.tignioj.sqlutil.wrapper.GenericUtils.*;

/**
 * 封装了基本的增删改查和单表条件查询
 *
 * @param <T>
 */
public class BaseDaoImpl<T> implements BaseDao<T> {
    private MyQueryRunnerImpl qr = new MyQueryRunnerImpl();
    private Class<T> clazz;
    private String tableName;

    public MyQueryRunnerImpl getQr() {
        return qr;
    }

    @Override
    public List<T> getAll(boolean hasChildren) throws SQLException {
        //获取表的名字
        String sql = "select * from " + tableName;
        List<T> query = qr.query(sql, clazz, hasChildren);
        return query;
    }

    @Override
    public List<T> getAllLimit(T t, int currentPageNumber, int itemCountPerPage, boolean hasChildren) throws SQLException {
        HashMap<String, Object> columnNameInSQLAndValueFromBeanHashMap = getColumnNameInSQLAndValueFromBeanHashMap(t);
        String sql = "select * from " + tableName;
        ArrayList arrayList = new ArrayList();

        if (columnNameInSQLAndValueFromBeanHashMap != null && columnNameInSQLAndValueFromBeanHashMap.size() != 0) {
            sql += " where ";
            for (Map.Entry<String, Object> entry : columnNameInSQLAndValueFromBeanHashMap.entrySet()) {
                if (entry.getValue() == null || "".equals(entry.getValue())) {
                    continue;
                }
                sql += "`" + entry.getKey() + "` like ?" + " and ";
                arrayList.add("%" + entry.getValue() + "%");
            }
            sql = getStringBefore(sql, "and");
        }
        //说明没有条件传入，应该把where去掉
        if (arrayList.size() == 0) {
            sql = getStringBefore(sql, "where");
        }

        sql += " limit ?,?";
        if (currentPageNumber <= 0) {
            currentPageNumber = 1;
        }
        arrayList.add((currentPageNumber - 1) * itemCountPerPage);
        arrayList.add(itemCountPerPage);
        Object[] params = arrayList.toArray();
        System.out.println(sql);
        List<T> query = qr.query(sql, params, clazz, hasChildren);
        return query;
    }

    @Override
    public List<T> getAllLimit(T t, int currentPageNumber, int itemCountPerPage) throws SQLException {
        return getAllLimit(t, currentPageNumber, itemCountPerPage, false);
    }

    @Override
    public List<T> getAllLimit(int currentPageNumber, int itemCountPerPage) throws SQLException {
        return getAllLimit(null, currentPageNumber, itemCountPerPage, false);
    }

    public BaseDaoImpl() {
//        // 获取当前new的对象的泛型的父类类型
        ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
//        // 获取第一个类型参数的真实类型
        this.clazz = (Class<T>) pt.getActualTypeArguments()[0];
        this.tableName = GenericUtils.getTableName(clazz);
    }

    @Override
    public List<T> getAll() throws SQLException {
        return getAll(false);
    }


    @Override
    public void addOne(T t) throws SQLException {
//        INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)
        HashMap<String, Object> columnNameAndValueInSQL = getColumnNameInSQLAndValueFromBeanHashMap(t);

        String sql = "insert into " + tableName + " (";
        String questionMark = "";
        ArrayList arrayList = new ArrayList();
        Set<Map.Entry<String, Object>> entries = columnNameAndValueInSQL.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
            if (entry.getValue() == null) {
                continue;
            }
            sql = sql + " `" + entry.getKey() + "`,";
            questionMark += "?,";
            arrayList.add(entry.getValue());
        }
        sql = sql.substring(0, sql.length() - 1);
        questionMark = questionMark.substring(0, questionMark.length() - 1) + ")";
        sql += ") VALUES (" + questionMark;
        System.out.println(sql);
        Object[] params = arrayList.toArray();
        boolean insert = qr.insert(sql, params);
        System.out.println(insert);
    }

    /**
     * 获取sql字段的名称以及sql的值
     *
     * @param t
     * @return
     */
    private HashMap<String, Object> getColumnNameInSQLAndValueFromBeanHashMap(T t) {
        if (t == null) {
            return null;
        }

        //一、获取对象的值和数据类型到hashMap中
        //1. 获取所有字段，如果字段类型是列表，则忽略
        //2. 如果属性值是非基本数据类型，则判断是不是外键
//        @ColumnInfo(referencedColumn = "name", referencedTable = FoodType.class)
        //  (1不是外键，则为保存filed的名字，
        //  (2 如果是外键，则保存外键的filed的名字
        //3. 根据2获取的filed名字拼接get方法
        //4. 调用对象以及外键的get方法获取value

        //二、封装对象的值到sql语句, 拼接sql语句
        HashMap<String, Object> columnNameAndValueInSQL = new HashMap<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        //1. 获取所有字段
        for (Field f : declaredFields) {
            //如果字段类型是列表，则忽略
            if (List.class.isAssignableFrom(f.getType()) || Set.class.isAssignableFrom(f.getType())) {
                continue;
            }

            //2. 如果属性值是非基本数据类型，则判断是不是外键
            //1)如果没有标注ColumnInfo(value="XXXX")
            //sql上的字段名, 用于拼接sql
            String columnNameInSQL = getColumnNameInSQL(f);

            //字段在对象上的值，通过调用get方法来获取，用于拼接sql;
            Object value;

            //java上的字段名，用于获取真实的get方法
            String fieldNameInJava = getGetterNameFromField(f);
            try {
                Method m = clazz.getDeclaredMethod(fieldNameInJava);
                //从Object中获取到成员值
                value = m.invoke(t);
                //如果是外键, 那么此时的value应该为外键的对象，则应该进一步调用外键的字段
                if (isFKColumn(f)) {
                    value = getFkValueFromFKObject(f, value);
                }
                columnNameAndValueInSQL.put(columnNameInSQL, value);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if (columnNameAndValueInSQL.size() == 0) {
            return null;
        }
        return columnNameAndValueInSQL;
    }


    /**
     * 外键对象遍历所有字段，找到和field对应的列，获取其属性值
     * 凭借sql语句后面where xxx=?中的问号
     *
     * @param f
     * @param fkObject
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Object getFkValueFromFKObject(Field f, Object fkObject) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (fkObject == null) {
            return null;
        }
        ColumnInfo annoFormOriginalObject = f.getAnnotation(ColumnInfo.class);

        //外键对象被参照的属性
        String rfColumn = annoFormOriginalObject.referencedColumn();
        //外键对象类型
        Class rfClass = annoFormOriginalObject.referencedTable();

        //遍历外键表的所有字段，使其和rfColumn匹配，则可获获取外键对象的get方法
        for (Field rfF : rfClass.getDeclaredFields()) {
            ColumnInfo annoFromReferenceObject = rfF.getAnnotation(ColumnInfo.class);
            if (annoFromReferenceObject != null && rfColumn.equals(annoFromReferenceObject.value())) {
                String fieldNameInJava = getGetterNameFromField(rfF);
                Method fkMethod = rfClass.getDeclaredMethod(fieldNameInJava);
                //此处value已经是一个对象, 用外键的方法调用对象而获取值
                fkObject = fkMethod.invoke(fkObject);
            }
        }
        return fkObject;
    }

    /**
     * 根据主键删除
     *
     * @param t
     * @return
     * @throws SQLException
     */
    @Override
    public T delete(T t) throws SQLException {
//        HashMap<String, Object> columnNameInSQLAndValueFromBeanHashMap = getColumnNameInSQLAndValueFromBeanHashMap(t);
        String sql = "delete from " + tableName + " where ";
        HashMap<String, Object> pkMap = getPrimaryKeyAndValueMap(t);
        if (pkMap == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<String, Object> entry : pkMap.entrySet()) {
            sql += " " + entry.getKey() + " =?" + " and ";
            arrayList.add(entry.getValue());
        }
        int and = sql.lastIndexOf("and");
        if (and != -1) {
            sql = sql.substring(0, and);
            System.out.println(sql);
        }
        Object[] params = arrayList.toArray();
        boolean delete = qr.delete(sql, params);
        return null;
    }

    public HashMap<String, Object> getPrimaryKeyAndValueMap(T t) {
        Field[] declaredFields = clazz.getDeclaredFields();
        HashMap<String, Object> pkMap = new HashMap<>();

        String pkName = null;
        String pkNameInJava = null;
        Object pkValue = null;
        for (Field f : declaredFields) {
            ColumnInfo annotation = f.getAnnotation(ColumnInfo.class);
            if (annotation != null && annotation.isPrimaryKey()) {
                pkName = annotation.value().equals("") ? f.getName().toLowerCase() : annotation.value().toLowerCase();
                pkNameInJava = f.getName();
                if (pkNameInJava.length() > 1) {
                    pkNameInJava = pkNameInJava.substring(0, 1).toUpperCase() + pkNameInJava.substring(1);
                }
                try {
                    Method m = clazz.getDeclaredMethod("get" + pkNameInJava);
                    pkValue = m.invoke(t);
                    pkMap.put(pkName, pkValue);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return pkMap;
    }

    @Override
    public T update(T t) throws SQLException {
        String sql = "update " + tableName + " set ";
        HashMap<String, Object> columnNameInSQLAndValueFromBeanHashMap = getColumnNameInSQLAndValueFromBeanHashMap(t);
        HashMap<String, Object> pkMap = getPrimaryKeyAndValueMap(t);
        if (columnNameInSQLAndValueFromBeanHashMap == null || pkMap == null) {
            throw new SQLException("必须设置主键！, 请在主键加上ColumnInfo(value='主键在SQL中的字段名', isPrimaryKey=true)");
        }

        ArrayList arrayList = new ArrayList();
        for (Map.Entry<String, Object> entry : columnNameInSQLAndValueFromBeanHashMap.entrySet()) {
            if (pkMap.get(entry.getKey()) != null) {
                continue;
            }
            sql += "`" + entry.getKey() + "`=" + "?,";
            arrayList.add(entry.getValue());
        }

        sql = getStringBefore(sql, ",");
        sql += " where ";
        for (Map.Entry<String, Object> entry : pkMap.entrySet()) {
            sql += "`" + entry.getKey() + "`=?" + " and ";
            arrayList.add(entry.getValue());
        }
        sql = getStringBefore(sql, "and");
        Object[] params = arrayList.toArray();
        int update = qr.update(sql, params);
        return null;
    }

    public List<T> getByCondition(T t) throws SQLException {
        return getByCondition(t, false);
    }

    @Override
    public List<T> getByCondition(T t, boolean hasChildren) throws SQLException {
        HashMap<String, Object> columnNameInSQLAndValueFromBeanHashMap = getColumnNameInSQLAndValueFromBeanHashMap(t);
        if (columnNameInSQLAndValueFromBeanHashMap == null || columnNameInSQLAndValueFromBeanHashMap.size() == 0) {
            return getAll(false);
        }
        String sql = "select * from " + tableName + " where ";
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<String, Object> entry : columnNameInSQLAndValueFromBeanHashMap.entrySet()) {
            if (entry.getValue() == null || "".equals(entry.getValue())) {
                continue;
            }
            sql += "`" + entry.getKey() + "` like ?" + " and ";
            arrayList.add("%" + entry.getValue() + "%");
        }
        Object[] params = arrayList.toArray();
        sql = getStringBefore(sql, "and");
        System.out.println(sql);
        List<T> query = qr.query(sql, params, clazz, hasChildren);
        return query;
    }


}
