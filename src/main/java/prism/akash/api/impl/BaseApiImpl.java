package prism.akash.api.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.extend.BaseDataExtends;
import prism.akash.container.sqlEngine.engineEnum.conditionType;
import prism.akash.container.sqlEngine.engineEnum.groupType;
import prism.akash.container.sqlEngine.engineEnum.queryType;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.dataInteraction.BaseInteraction;
import prism.akash.tools.StringKit;
import prism.akash.tools.logger.CoreLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component("baseApiImpl")
public class BaseApiImpl extends BaseDataExtends implements BaseApi {

    @Autowired
    BaseInteraction baseInteraction;
    @Autowired
    CoreLogger coreLogger;

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
    public List<BaseData> selectBase(sqlEngine sqlEngine) {
        BaseData bd = sqlEngine.parseSql();
        return bd.get("select") == null ? null : baseInteraction.select(bd);
    }

    @Override
    @Transactional
    public int execute(String id, String executeData) {
        BaseData bd = this.getEngineData(id, executeData);
        return bd.get("executeSql") == null ? null : baseInteraction.execute(bd);
    }

    @Override
    @Transactional
    public int execute(sqlEngine sqlEngine) {
        return baseInteraction.execute(sqlEngine.parseSql());
    }

    @Override
    @Transactional
    public int insertData(String id, String executeData) {
        BaseData bd = super.invokeInsertData(id, executeData).parseSql();
        return bd.get("executeSql") == null ? null : baseInteraction.execute(super.invokeInsertData(id, executeData).parseSql());
    }

    @Override
    @Transactional
    public int insertInitData(String table, String executeData) {
        int suc = 0;
        String tid = StringKit.getUUID();
        BaseData insertTable = new BaseData();
        BaseData updateTable = new BaseData();
        String[] tables = table.split("#");
        //TODO: 获取系统内已有表信息
        List<BaseData> tableList = this.selectBase(new sqlEngine()
                .execute("cr_tables", "t")
                .queryBuild(queryType.and, "t", "@code", conditionType.EQ, groupType.DEF, tables[0])
                .selectFin(""));
        if(tableList.size() > 0){
            tid = tableList.get(0).getString("id");
            //TODO: 执行数据更新
            updateTable.put("executeSql", "update cr_tables set code = '" + tables[0] + "',name = '" + tables[1] + "' where id = '" + tid + "'");
            suc = baseInteraction.execute(updateTable);
        }else{
            insertTable.put("executeSql",
                    "INSERT INTO cr_tables (id,code,name,state) VALUES ('" + tid + "','" + tables[0] + "','" + tables[1] + "',1)");
            suc = baseInteraction.execute(insertTable);
        }
        if (suc == 1) {
            //TODO: 新增或更新表成功后执行
            coreLogger.reCordLogger("0", tables[0], tableList.size() > 0 ? "2" : "0", tid, tableList.size() > 0 ? tid : "");

            //TODO: 同步字段表数据时，会批量更新先前数据状态为禁用（误操作保护）后重新提交创建
            baseInteraction.execute(new sqlEngine()
                    .execute("cr_field", "c")
                    .updateData("@state", "0")
                    .queryBuild(queryType.and, "c", "@tid", conditionType.EQ, groupType.DEF, tid)
                    .updateFin("").parseSql());

            //TODO: 新增表成功
            LinkedHashMap<String, Object> params = JSONObject.parseObject(executeData, new TypeReference<LinkedHashMap<String, Object>>() {
            });

            //TODO: 方法迁移
            List<BaseData> fetch = new ArrayList<>();
            String keys = "id,code,name,tid,type,size,sorts,state";
            int sorts = 1;
            for (String key : params.keySet()) {
                String[] dataAttribute = params.get(key).toString().split("\\|\\|");
                BaseData fe = new BaseData();
                String fid = StringKit.getUUID();
                fe.put("id", fid);
                fe.put("code", key);
                fe.put("name", dataAttribute[0]);
                fe.put("type", dataAttribute.length > 0 ? dataAttribute[1] : "");
                fe.put("size", dataAttribute.length > 1 ? dataAttribute[2] : "0.0");
                fe.put("tid", tid);
                fe.put("state", 1);
                fe.put("sorts", sorts);
                fetch.add(fe);
                //TODO: 写入数据列操作历史记录
                coreLogger.reCordLogger("1", tables[0], "0", fid, "");
                sorts++;
            }
            //TODO: 执行新增
            suc = baseInteraction.execute(new sqlEngine()
                    .execute("cr_field", "cf")
                    .insertFetchPush(JSON.toJSONString(fetch),
                            keys.substring(0, keys.length()))
                    .insertFin("")
                    .parseSql());
        }
        return suc;
    }
}
