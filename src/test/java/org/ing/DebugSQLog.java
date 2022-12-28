package org.ing;





import com.mysql.jdbc.ResultSetImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhongming
 * @since 2022/7/25
 */
public class DebugSQLog {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/imall?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai",
                "root",
                "1234");
        PreparedStatement preparedStatement = connection.prepareStatement("select * from user ");
        ResultSet resultSet = preparedStatement.executeQuery();

        List<Map<String, Object>> list = new ArrayList<>();
        final ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < columnCount; i++) {
                map.put(metaData.getColumnName(i+1), resultSet.getObject(i+1));
            }
            list.add(map);
        }


        System.out.println(((ResultSetImpl) resultSet).getUpdateCount() + "=======" + resultSet + "===columnNames:" + list);
        preparedStatement.close();
        connection.close();
    }
}
