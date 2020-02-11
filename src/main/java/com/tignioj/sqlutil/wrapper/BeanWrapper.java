package com.tignioj.sqlutil.wrapper;


import com.tignioj.sqlutil.wrapper.anno.ColumnInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 将ResultSet封装成bean
 * 1. 利用反射遍历T的所有成员
 * 2. 将成员对应的名称和类型封装进去
 * 3. 如果成员上面有注解ColumnInfo(value="名称")则使用注解上的名称用于获取
 * 4. 如果成员是自定义对象则封装自定义对象
 *
 * @param <T>
 */
public class BeanWrapper<T> {
    /**
     * 封装结果集到List
     *
     * @param resultSet   结果集
     * @param c           封装的结果集类型
     * @param connection  SQL连接
     * @param hasChildren 是否封装子类
     * @param <T>         返回的结果集合类型
     * @return
     */
    public static <T> List<T> wrapAll(ResultSet resultSet, Class c, Connection connection, boolean hasChildren) {
        ArrayList<T> results = new ArrayList();
        try {
            while (resultSet.next()) {
                //待封装的一个对象
                T one = null;
                //处理一个结果集
                one = wrapOne(resultSet, c, connection, hasChildren);
                results.add(one);
            }
            return results;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 封装一个对象
     * 1. 反射获取指定类的所有成员变量, 和成员变量类型，从而确定调用resultSet的.getXXX的XXX类型
     * 比如，在Java的Entity中User.username为Stirng, 则应该调用resultSet.getString("username")获取其值
     * 2. 如果成员变量上有ColumnInfo(value="xxx")注解，则获取xxx的值，
     * 调用resultSet.getString(xxx)获取应封装的值
     * <p>
     * 3. 调用成员的setXXX方法封装对象
     *
     * @param resultSet   一行结果集
     * @param aClass      封装对象类型
     * @param connection
     * @param hasChildren
     * @param <T>         返回对象类型
     * @return
     */
    public static <T> T wrapOne(ResultSet resultSet, Class<?> aClass, Connection connection, boolean hasChildren) {
        try {
            /**
             * 步骤
             * 1. 用匿名构造函数构造一个新的对象
             * 2. 遍历每一个java属性，获取sqlColumnName和javaTypeName
             *  如果没有ColumnInfo注解，则为sqlColumnName为java该字段的名称
             * 3. sqlColumnName用于决定调用resultSet的什么字段, 存在于ColumnInfo(value="xxx")的xxx中, 从而拿到值
             * 4. javaTypeName用于决定调用resultSet的什么方法
             * 5. 3-4拿到数据库的值，设为Object,准备封装
             * 6. 获取java属性的类型，从而拿到set方法
             *  1）获取真实的属性名称
             *      field.getName()
             *  2）获取真实的属性类型
             *      field.getType();
             *  3）根据1)2)获取set方法
             *
             *
             */


//             * 1. 用匿名构造函数构造一个新的对象
            T o = (T) aClass.getDeclaredConstructor().newInstance();

            //遍历每一个字段以调用对象的set方法,
            //每遍历获取一个字段就调用一次set方法给object赋值
            for (Field f : aClass.getDeclaredFields()) {
                //1. 获取bean所有字段名称, 如果有注解，则获取注解名称， 保存在sQLColumnMap中
                //2. 根据java数据类型调用resultSet对应获取数据方法
                Object value = invokeSQLGetMethodByJavaType(resultSet, f);


                //3. 从resultSet中拿到值后，利用反射调用java的set方法给对象赋值
                //(1. 先获取真实的setXxx名称, 一般写法是setXxx, 比如setUserName,
                // 那么我们就把java字段的第一个字母大写, 然后用set拼接，得到完整的方法名
                String methodName = GenericUtils.getSetterNameFromField(f);

                //(2. 获取方法
                Method m = aClass.getDeclaredMethod(methodName, f.getType());

                //4. 如果set方法不是基本数据类型，有外部决定是否继续封装
                // 一般不是基本数据类型，那么这个字段应该和其他表格相关, 则可能会有外键的存在
                if (hasChildren) {
                    //通过注解获取外键的名称，表的名称, 关联的对象类型
                    ColumnInfo annotation = f.getAnnotation(ColumnInfo.class);

                    if (annotation != null && !annotation.referencedTable().equals(Void.class)) {
                        String tableName = null;
                        String columnName = null;
                        Object columnValue;


                        Class referencedTable = annotation.referencedTable();
                        String className = GenericUtils.getTableName(referencedTable);
                        columnName = annotation.referencedColumn().equals("") ? className : annotation.referencedColumn();
                        tableName = GenericUtils.getTableName(referencedTable);
                        /**
                         * 解决思路
                         * 1. 获取指定类型的类，遍历成员，判断谁的注解中和本注解一一对应
                         *  (1. 遍历Food成员所有注解
                         *  (2. 如果@ColumnName.referenceColumn, 直接调用resultSet.getString(referenceColumn)方法获取value
                         *  (3. 将值赋值给?
                         */
                        //
                        //1. 遍历成员, 根据columnName查找外键表上谁的注解value是参考表的referenceColumn值
//                        columnValue = getColumnValueFromReferenceTable(resultSet, f);
                        columnValue = getColumnValue(resultSet, f);

                        String sql = "select * from " + tableName + " where " + columnName + " =?";
                        PreparedStatement pstmt = connection.prepareStatement(sql);
                        //5. 赋值
                        pstmt.setObject(1, columnValue);
                        ResultSet childResultSet = pstmt.executeQuery();

                        //6. 如果子类非列表，则获取第一个值
                        List<Object> objects = BeanWrapper.wrapAll(childResultSet, referencedTable, connection, false);
                        if (!List.class.isAssignableFrom(f.getType())) {
                            if (objects.size() != 0) {
                                value = objects.get(0);
                            }
                        } else {
                            value = objects;
                        }
                    }

                }
                /**
                 * 调用方法给对象赋值
                 */
                m.invoke(o, value);
            }

            //返回对象
            return o;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getColumnValue(ResultSet resultSet, Field f) throws SQLException {
        Object columnValue;
        ColumnInfo anno = f.getAnnotation(ColumnInfo.class);
        if (anno == null) {
            columnValue = resultSet.getObject(f.getName().toLowerCase());
        } else {
            columnValue = resultSet.getObject(anno.value());
        }
        return columnValue;
    }


    private static void invokeSQLSetMethodByJavaType(PreparedStatement pstmt, Object value) throws SQLException {
        switch (value.getClass().getSimpleName()) {
            case "int":
                pstmt.setInt(1, (Integer) value);
                break;
            case "java.lang.Integer":
                pstmt.setInt(1, (Integer) value);
                break;
            case "double":
                pstmt.setDouble(1, (Double) value);
                break;
            case "java.lang.Double":
                pstmt.setDouble(1, (Double) value);
                break;
            case "java.lang.String":
                pstmt.setString(1, (String) value);
                break;
            case "java.util.Date":
                pstmt.setDate(1, (Date) value);
                break;
            default:
        }

    }

    /**
     * 从结果集中获取第三方表的字段
     * @param resultSet
     * @param f 参照的字段, 从中可以获取注解的外键表信息，外键column信息
     * @return
     * @throws SQLException
     */
    private static Object getColumnValueFromReferenceTable(ResultSet resultSet, Field f) throws SQLException {
        ColumnInfo annotation = f.getAnnotation(ColumnInfo.class);
        if (annotation == null || annotation.referencedTable().equals(Void.class) || annotation.referencedColumn().equals("")) {
            return null;
        }
        String referenceColumn = annotation.referencedColumn();
        Class referencedTable = annotation.referencedTable();

        //遍历外键列，如果和传入的columnName相同，则就是该列
        for (Field sf : referencedTable.getDeclaredFields()) {
            ColumnInfo annotation1 = sf.getAnnotation(ColumnInfo.class);
            //2. 如果@ColumnName.fkColumnName, 直接调用resultSet.getString(fkColumnName)方法获取value
            if (annotation1 != null && annotation1.value().equals(referenceColumn)) {
                return invokeSQLGetMethodByJavaType(resultSet, sf);
            }
        }
        return null;
    }



    /**
     * 根据field的数据类型来决定调用resultSet的getXXX方法
     * 比如field为String, 则调用resultSet.getStirng(columnName)
     *
     * @param resultSet
     * @param f
     * @return
     * @throws SQLException
     */
    private static Object invokeSQLGetMethodByJavaType(ResultSet resultSet, Field f) throws SQLException {
        String sqlColumnName = GenericUtils.getColumnNameInSQL(f);
        String javaTypeName = f.getType().getName();
        Object value = null;
        switch (javaTypeName) {
            case "int":
                value = resultSet.getInt(sqlColumnName);
                break;
            case "java.lang.Integer":
                value = resultSet.getInt(sqlColumnName);
                break;
            case "double":
                value = resultSet.getDouble(sqlColumnName);
                break;
            case "java.lang.Double":
                value = resultSet.getDouble(sqlColumnName);
                break;
            case "java.lang.String":
                value = resultSet.getString(sqlColumnName);
                break;
            case "java.util.Date":
                value = resultSet.getDate(sqlColumnName);
                break;
                //fix support boolean
            case "java.lang.Boolean":
                value = resultSet.getBoolean(sqlColumnName);
                break;
            default:
        }
        return value;
    }


}
