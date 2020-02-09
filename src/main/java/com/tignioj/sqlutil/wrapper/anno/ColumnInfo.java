package com.tignioj.sqlutil.wrapper.anno;

import com.mchange.v1.util.DebugUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记在Entity上的注解，对应的是sql的column字段名称
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnInfo {

    /**
     * 被参考表的列表名称
     *
     * @return
     */
    Class referencedTable() default Void.class;

    /**
     * 是否为主键
     * @return
     */
    boolean isPrimaryKey() default false;

    /**
     * 外键的在被参考表的字段名称, 这个值是数据库上的字段名称
     * 注意：如果被参考表的字段使用了value注解，那么该注解需要和被参考表的value相同
     *
     * 比如user.address 参考 Address.id1
     * 当Address.id1没有注解时
     *      那么在user.address上的注解为@ColumnInfo(referencedColumn="id1")
     * 当Address.id1设置了@ColumnName(value="abc")
     *      那么在user.address上的注解为@ColumnInfo(referencedColumn="abc")
     * @return
     */
    String referencedColumn() default "";

    /**
     * 本字段的名称
     * @return
     */
    String value() default "";
}
