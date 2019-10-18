package prism.akash.tools.asyncInit;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.stereotype.Component;
import prism.akash.container.BaseData;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 同步信息初始化
 *                --  每天凌晨00:30分扫描全库表及字段进行数据同步整合
 */
@Component
public class AsyncInitData {

    private DruidDataSource getDataSource(){
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3852/testAkash?characterEncoding=utf8&useSSL=false&serverTimezone=UTC");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername("root");
        dataSource.setPassword("chhu2017");
        dataSource.setInitialSize(1);
        dataSource.setMinIdle(1);
        dataSource.setMaxActive(20);
        //连接泄漏监测
        dataSource.setRemoveAbandoned(true);
        dataSource.setRemoveAbandonedTimeout(30);
        //配置获取连接等待超时的时间
        dataSource.setMaxWait(20000);
        //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        dataSource.setTimeBetweenEvictionRunsMillis(20000);
        //防止过期
        dataSource.setValidationQuery("SELECT 'x'");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(true);
        return dataSource;
    }

    public List<BaseData> asyncInitDataBase(){
        List<BaseData> tableArray = new ArrayList<>();
        Connection con = null;
        try {
            con = this.getDataSource().getConnection();
            DatabaseMetaData dbMetaData = con.getMetaData();

            ResultSet rs = dbMetaData.getTables(null, null, null, new String[]{"TABLE"});
            while (rs.next()) {
                if (rs.getString("TABLE_CAT").equals("testakash")) {
                    BaseData table = new BaseData();
                    table.put("code",rs.getString("TABLE_NAME"));
                    table.put("name",rs.getString("REMARKS"));

                    ResultSet rsColimns = dbMetaData.getColumns(null, "", rs.getString("TABLE_NAME"), "");
                    BaseData colimns = new BaseData();
                    while (rsColimns.next()) {
                        colimns.put(rsColimns.getString("COLUMN_NAME"),rsColimns.getString("REMARKS"));
//                        colimns.put("columnType",rsColimns.getString("TYPE_NAME"));
//                        colimns.put("columnSize",rsColimns.getString("COLUMN_SIZE"));
                    }
                    table.put("colimns",colimns);
                    tableArray.add(table);
                }
            }

            con.close();
            //TODO : 关闭清空连接,等待GC回收
            con = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tableArray;
    }
}
