package com.tignioj.sqlutil.configure;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SQLUtilsConfig {
    public static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 格式化日期
     * @param date
     * @return
     */
    public static String format(Date date) {
        return format(date, DATETIME_FORMAT);
    }

    /**
     * 格式化日期
     * @param date 日期对象
     * @param pattern 格式字符串, 比如 "yyyy-MM-dd HH:mm:ss"
     * @return
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(pattern).format(date);
    }
}
