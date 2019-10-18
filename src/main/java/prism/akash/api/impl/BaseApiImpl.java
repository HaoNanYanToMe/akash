package prism.akash.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.extend.BaseDataExtends;
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
        return baseInteraction.select(this.getEngineData(id,executeData));
    }

    @Override
    public Map<String, Object> selectPage(String id, String executeData) {
        Map<String, Object> reObj = new ConcurrentHashMap<>();
        BaseData selectPage = this.getEngineData(id,executeData);
        reObj.put("data", baseInteraction.select(selectPage));
        reObj.put("total", baseInteraction.selectNums(selectPage));
        return reObj;
    }

    @Override
    public int execute(String id, String executeData) {
        return baseInteraction.execute(this.getEngineData(id,executeData));
    }

    @Override
    public List<BaseData> selectBase(sqlEngine sqlEngine) {
        return baseInteraction.select(sqlEngine.parseSql());
    }

    @Override
    public int insertData(String id, String executeData) {
        return  baseInteraction.execute(super.invokeInsertData(id,executeData).parseSql());
    }

    @Override
    public int insertInitData(String table, String executeData) {
        String tid = UUID.randomUUID().toString().replaceAll("-","");
        BaseData insertTable = new BaseData();
        insertTable.put("executeSql","INSERT INTO tableArray (id,code,name) VALUES ('"+tid+"','"+table.split("#")[0]+"','"+table.split("#")[1]+"')");
        int suc = baseInteraction.execute(insertTable);
        if(suc == 1){
            //新增表成功
            suc = baseInteraction.execute(super.invokeDataInteraction(new sqlEngine().execute("columnArray",tid),"a20761d69ad64bd3a111525a4c6ddbca",executeData,false).parseSql());
        }
        return suc;
    }
}
