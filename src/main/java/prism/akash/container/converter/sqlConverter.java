package prism.akash.container.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.engineEnum.conditionType;
import prism.akash.container.sqlEngine.engineEnum.groupType;
import prism.akash.container.sqlEngine.engineEnum.queryType;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.tools.StringKit;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * TODO : sql引擎编辑转换器
 */
@Component
public class sqlConverter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    BaseApi baseApi;

    //
    Boolean exist = false;

    Integer sort = 0;
    Integer childSort = 0;

    String engineId;

    String childId;

    public BaseData checkCodeExist(String code) {
        List<BaseData> exist = baseApi.selectBase(new sqlEngine()
                .execute("cr_engine", "c")
                .appointColumn("c", groupType.DEF, "id")
                .queryBuild(queryType.and, "c", "@code", conditionType.EQ, groupType.DEF, code)
                .queryBuild(queryType.and, "c", "@state", conditionType.EQ, groupType.DEF, "0").selectFin(""));
        return exist.size() > 0 ? exist.get(0) : null;
    }

    public String initConverter(String name, String code, String note) {
        //实例初始化
        sort = 0;
        exist = false;
        BaseData converter = checkCodeExist(code);
        if (converter == null) {
            engineId = StringKit.getUUID();
            // TODO : 逻辑引擎不存在则执行新建
            sqlEngine addEngine = new sqlEngine().execute("cr_engine", "")
                    .addData("@id", engineId)
                    .addData("@name", name)
                    .addData("@code", code)
                    .addData("@note", note)
                    .addData("@state", "0")
                    .addData("@executeVail", "0")
                    .insertFin("");
            baseApi.execute(addEngine);
        } else {
            // TODO : 逻辑引擎存在则变更指令标识为true
            exist = true;
            engineId = converter.getString("id");
        }
        return engineId;
    }

    public String execute(boolean isChild, String data) {
        String id = StringKit.getUUID();
        boolean child = false;
        sqlEngine addExecute = new sqlEngine().execute("cr_engineexecute", "")
                .addData("@id", id)
                .addData("@eid", isChild ? childId : engineId)
                .addData("@state", "1")
                .addData("@sorts", isChild ? childSort + "" : sort + "");
        LinkedHashMap<String, Object> params = JSONObject.parseObject(data, new TypeReference<LinkedHashMap<String, Object>>() {
        });
        //TODO : 确认传入字段存在
        for (String key : params.keySet()) {
            if (key.equals("childList")) {
                childId = id;
                child = true;
                // TODO : 子查询内循环
                JSONArray ja = JSONArray.parseArray(params.get(key).toString());
                for (int j = 0; j < ja.size(); j++) {
                    JSONObject jo = ja.getJSONObject(j);
                    execute(true,jo.toJSONString());
                }
            } else {
                addExecute.addData("@" + key, params.get(key).toString());
            }
        }
        addExecute.addData("@isChild", child ? "1" : "0");
        addExecute.insertFin("");
        baseApi.execute(addExecute);
        if(isChild){
            childSort++;
        }else{
            sort++;
        }
        return id;
    }
}
