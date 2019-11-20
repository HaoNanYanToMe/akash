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

    /**
     * 检查当前引擎Code值是否已被使用
     * @param code
     * @return
     */
    public BaseData checkCodeExist(String code) {
        List<BaseData> exist = baseApi.selectBase(new sqlEngine()
                .execute("cr_engine", "c")
                .appointColumn("c", groupType.DEF, "id")
                .queryBuild(queryType.and, "c", "@code", conditionType.EQ, groupType.DEF, code)
                .queryBuild(queryType.and, "c", "@state", conditionType.EQ, groupType.DEF, "0").selectFin(""));
        return exist.size() > 0 ? exist.get(0) : null;
    }

    /**
     * 初始化逻辑执行引擎
     * @param name      引擎名称
     * @param code      引擎唯一标识CODE
     * @param note      引擎备注信息
     * @return
     */
    public String initConverter(ConverterData initData,String name, String code, String note) {
        //实例初始化
        initData.setSort(0);
        initData.setExist(false);

        BaseData converter = checkCodeExist(code);
        if (converter == null) {
            initData.setEngineId(StringKit.getUUID());
            // TODO : 逻辑引擎不存在则执行新建
            sqlEngine addEngine = new sqlEngine().execute("cr_engine", "")
                    .addData("@id", initData.getEngineId())
                    .addData("@name", name)
                    .addData("@code", code)
                    .addData("@note", note)
                    .addData("@state", "0")
                    .addData("@executeVail", "0")
                    .insertFin("");
            baseApi.execute(addEngine);
        } else {
            // TODO : 逻辑引擎存在则变更指令标识为true
            initData.setExist(true);
            initData.setEngineId(converter.getString("id"));
        }
        return initData.getEngineId();
    }

    /**
     * 核心逻辑节点载入
     * @param isChild    当前节点是否使用子查询模式
     * @param data       当前节点预处理JSON值
     * @return
     */
    public String execute(ConverterData initData,boolean isChild, String data) {
        //如果当前为新创建的引擎（exist = false）且engineId已生成（引擎已创建成功）
        if(!initData.getExist() && initData.getEngineId() != null){
            String id = StringKit.getUUID();
            boolean child = false;
            sqlEngine addExecute = new sqlEngine().execute("cr_engineexecute", "")
                    .addData("@id", id)
                    .addData("@eid", isChild ? initData.getChildId() : initData.getEngineId())
                    .addData("@state", "1")
                    .addData("@sorts", isChild ? initData.getChildSort() + "" : initData.getSort() + "");
            LinkedHashMap<String, Object> params = JSONObject.parseObject(data, new TypeReference<LinkedHashMap<String, Object>>() {
            });
            //TODO : 确认传入字段存在
            for (String key : params.keySet()) {
                if (key.equals("childList")) {
                    initData.setChildId(id);
                    child = true;
                    // TODO : 子查询内循环
                    JSONArray ja = JSONArray.parseArray(params.get(key).toString());
                    for (int j = 0; j < ja.size(); j++) {
                        JSONObject jo = ja.getJSONObject(j);
                        execute(initData,true,jo.toJSONString());
                    }
                } else {
                    addExecute.addData("@" + key, params.get(key).toString());
                }
            }
            addExecute.addData("@isChild", child ? "1" : "0");
            addExecute.insertFin("");
            baseApi.execute(addExecute);
            if(isChild){
                initData.setChildSort(initData.getChildSort()+1);
            }else{
                initData.setSort(initData.getSort()+1);
            }
            return id;
        }else{
            return null;
        }
    }
}
