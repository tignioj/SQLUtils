package com.tignioj.sqlutil.wrapper;

import com.tignioj.sqlutil.wrapper.anno.ColumnInfo;
import com.tignioj.sqlutil.wrapper.anno.TableInfo;

import java.lang.reflect.Field;
import java.sql.SQLException;

public class GenericUtils {
    public static String getTableName(Class clazz) {
        String tableName = clazz.getSimpleName().toLowerCase();
        TableInfo annotation = (TableInfo) clazz.getAnnotation(TableInfo.class);
        if (annotation != null) {
            tableName = annotation.tableName();
        }
        return tableName;
    }

    public static String getSetterNameFromField(Field f) {
        String fieldNameInJava = f.getName();
        String methodName;
        if (fieldNameInJava.length() > 1) {
            methodName = "set" + fieldNameInJava.substring(0, 1).toUpperCase() + fieldNameInJava.substring(1);
        } else {
            methodName = "set" + fieldNameInJava.toUpperCase();
        }
        return methodName;
    }

    /**
     * 根据field获取其get方法，
     * 如String userName
     * 则返回getUserName
     * @param f
     * @return
     */
    public static String getGetterNameFromField(Field f) {
        String fieldNameInJava = f.getName();
        if (fieldNameInJava.length() > 1) {
            fieldNameInJava = "get" +  fieldNameInJava.substring(0, 1).toUpperCase() + fieldNameInJava.substring(1);
        } else {
            fieldNameInJava = "get" + fieldNameInJava.toUpperCase();
        }
        return fieldNameInJava;
    }

    /**
     * 获取java字段对应的sql字段名称
     * 有注解则获取注解上的@ColunmInfo(value="xxx")中的XXX，否则返回f.getName()
     *
     * @param f java bean字段
     * @return
     */
    public static String getColumnNameInSQL(Field f) {
        String columnNameInSQL;
        ColumnInfo annotation = f.getAnnotation(ColumnInfo.class);
        if (annotation != null && !"".equals(annotation.value())) {
            columnNameInSQL = annotation.value();
        } else {
            columnNameInSQL = f.getName();
        }
        return columnNameInSQL;
    }


    /**
     * 是否为包含外键的字段
     *
     * @param f
     * @return
     */
    public static boolean isFKColumn(Field f) {
        ColumnInfo annotation = f.getAnnotation(ColumnInfo.class);
        return annotation != null && !"".equals(annotation.referencedColumn()) && !annotation.referencedTable().equals(Void.class);
    }

    /**
     * 截取字符串
     *
     * @param originalString
     * @param stringBefore
     * @return
     * @throws Exception
     */
    public static String getStringBefore(String originalString, String stringBefore) throws SQLException {
        if (stringBefore == null || "".equals(stringBefore)) {
            throw new SQLException("sql为空");
        }
        int i = originalString.lastIndexOf(stringBefore);
        if (i == -1) {
            return originalString;
        } else {
            return originalString.substring(0, i);
        }
    }

}
