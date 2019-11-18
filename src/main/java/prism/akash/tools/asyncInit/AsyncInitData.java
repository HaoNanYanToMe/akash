package prism.akash.tools.asyncInit;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 同步信息初始化
 *                --  每天凌晨00:30分扫描全库表及字段进行数据同步整合
 */
@Component
public class AsyncInitData {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.driver-class-name}")
    private String DriverClassName;

    @Value("${spring.datasource.username}")
    private String usernName;

    @Value("${spring.datasource.password}")
    private String password;

    @Autowired
    BaseApi baseApi;

    //数据源连接
    private DruidDataSource getDataSource(){
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName(DriverClassName);
        dataSource.setUsername(usernName);
        dataSource.setPassword(password);
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

    //相关数据信息同步
    public List<BaseData> asyncInitDataBase(){
        List<BaseData> tableArray = new ArrayList<>();
        Connection con = null;
        try {
            con = this.getDataSource().getConnection();
            DatabaseMetaData dbMetaData = con.getMetaData();
            //TODO : 获取目前使用的数据库名称
            String dataBase = url.split(":")[3].split("/")[1].split("\\?")[0];
            ResultSet rs = dbMetaData.getTables(dataBase.toLowerCase(), null, null , new String[]{"TABLE"});
            while (rs.next()) {
                    BaseData table = new BaseData();
                    table.put("code", rs.getString("TABLE_NAME"));
                    table.put("name", rs.getString("REMARKS"));
                    ResultSet rsColimns = dbMetaData.getColumns(dataBase, null, rs.getString("TABLE_NAME"), "%");
                    BaseData colimns = new BaseData();
                    while (rsColimns.next()) {
                        String cname = rsColimns.getString("COLUMN_NAME");
                        colimns.put(cname, rsColimns.getString("REMARKS")+"||"+rsColimns.getString("TYPE_NAME")+"||"+rsColimns.getString("COLUMN_SIZE"));
                    }
                    table.put("colimns", colimns);
                    tableArray.add(table);
            }

            con.close();
            //TODO : 关闭清空连接,等待GC回收
            con = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tableArray;
    }

    /**
     * 系统全量同步更新
     * TODO : 用于更新指定库整库数据
     *
     * @return
     */
    public Map<String, Object> executor() {
        Map<String, Object> re = new ConcurrentHashMap<>();
        int suc = 0;
        List<Object> fail = new ArrayList<>();
        /**
         * 获取本次需要同步的数据
         */
        List<BaseData> asy = this.asyncInitDataBase();

        for (BaseData t : asy) {
            if (baseApi.insertInitData(t.get("code") + "#" + t.get("name") + " ",
                    JSON.toJSONString(t.get("colimns"))) > 0) {
                suc++;
            } else {
                fail.add(t.get("code"));
            }
        }
        re.put("success", suc);
        re.put("failed", asy.size() - suc);
        re.put("failedList", fail);
        return re;
    }
}
