package prism.akash.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.extend.BaseDataExtends;
import prism.akash.container.sqlEngine.engineEnum.conditionType;
import prism.akash.container.sqlEngine.engineEnum.groupType;
import prism.akash.container.sqlEngine.engineEnum.queryType;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.dataInteraction.BaseInteraction;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component("baseApiImpl")
public class BaseApiImpl extends BaseDataExtends implements BaseApi {

    @Autowired
    BaseInteraction baseInteraction;

    private BaseData getEngineData(String id, String executeData){
        return super.invokeDataInteraction(
                new sqlEngine(),
                id, executeData,
                false)
                .parseSql();
    }

    @Override
    public List<BaseData> select(String id, String executeData) {
        BaseData bd = this.getEngineData(id, executeData);
        return bd.get("select") == null ? null : baseInteraction.select(bd);
    }

    @Override
    public Map<String, Object> selectPage(String id, String executeData) {
        Map<String, Object> reObj = new ConcurrentHashMap<>();
        BaseData selectPage = this.getEngineData(id,executeData);
        if (selectPage.get("select") != null) {
            reObj.put("data", baseInteraction.select(selectPage));
        }
        if (selectPage.get("totalSql") != null) {
            reObj.put("total", baseInteraction.selectNums(selectPage));
        }
        return reObj;
    }

    @Override
    public int execute(String id, String executeData) {
        BaseData bd = this.getEngineData(id, executeData);
        return bd.get("executeSql") == null ? null : baseInteraction.execute(bd);
    }

    @Override
    public List<BaseData> selectBase(sqlEngine sqlEngine) {
        BaseData bd = sqlEngine.parseSql();
        return bd.get("select") == null ? null : baseInteraction.select(bd);
    }

    @Override
    public int execute(sqlEngine sqlEngine) {
        return baseInteraction.execute(sqlEngine.parseSql());
    }

    @Override
    public int insertData(String id, String executeData) {
        BaseData bd = super.invokeInsertData(id, executeData).parseSql();
        return bd.get("executeSql") == null ? null : baseInteraction.execute(super.invokeInsertData(id, executeData).parseSql());
    }

    @Override
    public int insertInitData(String table, String executeData) {
        int suc = 0;
        String tid = UUID.randomUUID().toString().replaceAll("-", "");
        BaseData insertTable = new BaseData();
        String[] tables = table.split("#");
        //TODO: 获取系统内已有表信息
        List<BaseData> tableList = this.selectBase(new sqlEngine()
                .execute("tableArray", "t")
                .queryBuild(queryType.and, "t", "@code", conditionType.EQ, groupType.DEF, tables[0])
                .selectFin(""));
        if(tableList.size() > 0){
            suc = 1;
            tid = tableList.get(0).getString("id");
        }else{
            insertTable.put("executeSql",
                    "INSERT INTO tableArray (id,code,name) VALUES ('" + tid + "','" + tables[0] + "','" + tables[1] + "')");
            suc = baseInteraction.execute(insertTable);
        }
        if (suc == 1) {
            //TODO: 同步字段表数据时，会批量删除后重新创建（保留缓存数据）,在重置同步完成后会清除缓存（缓存key为c_id）
            baseInteraction.execute(new sqlEngine()
                    .execute("columnArray", "c")
                    .queryBuild(queryType.and, "c", "@tid", conditionType.EQ, groupType.DEF, tid)
                    .deleteFin("").parseSql());

            //新增表成功
            suc = baseInteraction.execute(super.invokeDataInteraction(
                    new sqlEngine()
                            .execute("columnArray", tid),
                    "a20761d69ad64bd3a111525a4c6ddbca",
                    executeData,
                    false)
                    .parseSql());
        }
        return suc;
    }
}
