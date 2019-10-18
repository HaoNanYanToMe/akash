package prism.akash.api;

import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.sqlEngine;

import java.util.List;
import java.util.Map;

public interface BaseApi {

    List<BaseData> select(String id,String executeData);

    Map<String,Object> selectPage(String id,String executeData);

    int execute(String id,String executeData);

    //TODO ：仅限于基础表信息新增维护时使用
    int insertData(String id,String executeData);

    /**
     * TODO :  主要用于tableArray及Columns表的初始化
     * @param table    表名#备注
     * @param executeData
     * @return
     */
    int insertInitData(String table,String executeData);

    //TODO =======> 以下接口仅对内使用
    List<BaseData> selectBase(sqlEngine sqlEngine);
}
