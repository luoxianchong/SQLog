package org.ing.sql.log.transformer;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @author zhongming
 * @since 2022/12/28
 */
public class SQLInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SQLInterceptor.class);

    @RuntimeType
    public static ResultSet intercept(@This Object obj, // 目标对象
                                      @AllArguments Object[] args, // 注入目标方法的全部参数
                                      @SuperCall Callable<ResultSet> caller // 调用目标方法，必不可少哦
    ) throws Exception {
        String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        Method asSql = readMethod(obj.getClass(), "asSql");
        Object sql = Objects.nonNull(asSql) ? asSql.invoke(obj) : args[0];

        long start = System.currentTimeMillis();
        ResultSet resultSet = null;
        long effect = -1L;
        try {
            // 调用目标方法
            resultSet = caller.call();
        } finally {
            if (resultSet != null) {
                Method updateCount = readMethod(resultSet.getClass(), "getUpdateCount");
                effect = Objects.nonNull(updateCount) ? (Long) updateCount.invoke(resultSet) : -1L;
            }
            System.out.println(nowTime + "-elapse:[" + (System.currentTimeMillis() - start) + "ms]-sql：[" + sql + "]-effect rows：[" + effect + "]-result：" + readResult(resultSet));
        }
        return resultSet;
    }

    private static Method readMethod(Class<?> clazz, String methodName) {
        try {
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (methodName.equals(method.getName()) && method.getParameterCount() == 0) {
                    return method;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String readResult(ResultSet rs) {
        List<Map<String, Object>> list = new ArrayList<>();

        if (Objects.nonNull(rs)) {
            try {
                final ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i < columnCount; i++) {
                        map.put(metaData.getColumnName(i + 1), rs.getObject(i + 1));
                    }
                    list.add(map);
                }

                Method method = readMethod(rs.getClass(), "prev");
                while (Boolean.TRUE.equals(method.invoke(rs))){

                }
                return list.toString().substring(0, 4096);
            } catch (Exception ignored) {
            }
        }
        return list.toString();
    }

}