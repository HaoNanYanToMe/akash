package prism.akash.container.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.extend.BaseDataExtends;
import prism.akash.container.sqlEngine.engineEnum.conditionType;
import prism.akash.container.sqlEngine.engineEnum.groupType;
import prism.akash.container.sqlEngine.engineEnum.queryType;
import prism.akash.container.sqlEngine.engineEnum.sortType;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.tools.StringKit;
import prism.akash.tools.logger.CoreLogger;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * TODO : 逻辑引擎编辑转换器
 */
@Component
public class sqlConverter extends BaseDataExtends implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    BaseApi baseApi;


    @Autowired
    CoreLogger coreLogger;

    /**
     * 创建一个新的逻辑引擎
     * @param name               引擎名称
     * @param code               引擎唯一标识CODE
     * @param note               引擎备注信息
     * @param executeData        核心逻辑数据
     * @return
     */
    public ConverterData createBuild(String name, String code, String note, String executeData) {
        //初始化引擎创建工具
        ConverterData init = new ConverterData();
        // TODO : # 创建引擎时，code值及配参值必须通过数据校验！（强制-数据安全）
//        init.setErrorMsg(new ConverterValidator(executeData).verification());
        if(!StringKit.isSpecialChar(code) && init.getErrorMsg() == null){
            //创建实例化引擎
            initConverter(init, name, code, note);

            if (init.getExist()) {
                int newVersion = init.getVersion() + 1;
                // TODO : 更新当前引擎的版本号
                sqlEngine updateEngine = new sqlEngine();
                updateEngine.execute("cr_engine", "c")
                        .updateData("@version", newVersion + "")
                        .queryBuild(queryType.and, "c", "@id", conditionType.EQ, groupType.DEF, init.getEngineId())
                        .updateFin("");
                baseApi.execute(updateEngine);

                // TODO : 更新当前执行的引擎的版本号
                sqlEngine update = new sqlEngine();
                update.execute("cr_engineexecute", "c")
                        .updateData("@state", "0")
                        .updateData("@version", newVersion + "")
                        .queryBuild(queryType.and, "c", "@eid", conditionType.EQ, groupType.DEF, init.getEngineId())
                        .queryBuild(queryType.and, "c", "@version", conditionType.EQ, groupType.DEF, init.getVersion() + "")
                        .updateFin("");
                baseApi.execute(update);
            }

            //解析核心逻辑数据
            JSONArray coverArray = JSONArray.parseArray(executeData);
            for (int i = 0; i < coverArray.size(); i++) {
                execute(init, false, coverArray.getJSONObject(i).toJSONString());
            }

            paramBinding(init);
        }
        return init;
    }

    /**
     * 入参字段信息绑定
     * @param init     核心引擎数据对象
     * @return
     */
    private ConverterData paramBinding(ConverterData init){
        // TODO ：获取当前引擎的执行结果
        BaseData execute = super.invokeDataInteraction(new sqlEngine(), init.getEngineId(), "", false).parseSql();
        if (execute != null) {
            init.setExecute(execute);
            // TODO : 对指定的入参字段进行抽离另存
            if (execute.get("executeParam") != null){
                // TODO : 重新进行数据绑定
                this.bindFiled(execute.get("cr_engineparam") + "","cr_engineparam",init.getEngineId());

            }
            if(execute.get("outFiled") != null){
                // TODO : 输出字段绑定
                this.bindFiled(execute.get("outFiled") + "","cr_engineout",init.getEngineId());
            }
        }
        return init;
    }

    /**
     * 字段绑定方法（提取）
     * @param filed
     * @param table
     * @param eid
     */
    private void bindFiled(String filed,String table,String eid){
        String ver = getVersion(table,eid);
        for (String ac : filed.split(",")){
            if (!ac.equals("")){
                String [] codeAndName = ac.indexOf(" as ") > -1 ? ac.split(" as ") : ac.split("#");
                sqlEngine addParam = new sqlEngine().execute(table, "")
                        .addData("@id", StringKit.getUUID())
                        .addData("@name", ac.contains("#") ? codeAndName[1].trim() : "")
                        .addData("@code", codeAndName[0].trim())
                        .addData("@engineId", eid)
                        .addData("@version", ver)
                        .addData("@state", "1")
                        .insertFin("");
                baseApi.execute(addParam);
            }
        }
    }

    /**
     * 获取当前数据版本信息（如存在则将历史版本状态更新为禁用）
     * @param table
     * @param eid
     * @return
     */
    private String getVersion(String table,String eid){
        //获取版本信息
        List<BaseData> list =  baseApi.selectBase(new sqlEngine()
                .execute(table, "e")
                .appointColumn("e",groupType.DEF,"version")
                .queryBuild(queryType.and, "e", "@engineId", conditionType.EQ, null,eid)
                .queryBuild(queryType.and, "e", "@state", conditionType.EQ, null, "1")
                .dataSort("e", "version", sortType.DESC)
                .dataPaging("@0","@1")
                .selectFin(""));
        if (list.size() > 0){
            //更新状态
            baseApi.execute(new sqlEngine().execute(table, "c")
                    .updateData("@state",  "0")
                    .queryBuild(queryType.and, "c", "@engineId", conditionType.EQ, groupType.DEF, eid)
                    .updateFin(""));
            return  (Integer.parseInt(list.get(0).get("version") + "") + 1) + "";
        }else {
            return "0";
        }
    }

    /**
     * 检查当前引擎Code值是否已被使用
     *
     * @param code
     * @return
     */
    private BaseData checkCodeExist(String code) {
        List<BaseData> exist = baseApi.selectBase(new sqlEngine()
                .execute("cr_engine", "c")
                .appointColumn("c", groupType.DEF, "id")
                .queryBuild(queryType.and, "c", "@code", conditionType.EQ, groupType.DEF, code)
                .queryBuild(queryType.and, "c", "@engineType", conditionType.EQ, groupType.DEF, "0")
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
    private String initConverter(ConverterData initData, String name, String code, String note) {
        //实例初始化
        initData.setSort(0);
        initData.setExist(false);
        initData.setVersion(0);

        BaseData converter = checkCodeExist(code);
        if (converter == null) {
            initData.setEngineId(StringKit.getUUID());
            // TODO : 逻辑引擎不存在则执行新建
            sqlEngine addEngine = new sqlEngine().execute("cr_engine", "")
                    .addData("@id", initData.getEngineId())
                    .addData("@name", name)
                    .addData("@code", code)
                    .addData("@note", note)
                    .addData("@engineType", "0")
                    .addData("@state", "0")
                    .addData("@executeVail", "0")
                    .addData("@version", initData.getVersion() + "")
                    .insertFin("");
            int result = baseApi.execute(addEngine);
            if (result > 0)
                // TODO : 日志写入
                coreLogger.reCordLogger("0", "cr_engine", "0", initData.getEngineId(), initData.getVersion() + "");
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
    private String execute(ConverterData initData, boolean isChild, String data) {
        //如果当前engineId已生成（引擎已创建成功）
        if (initData.getEngineId() != null) {
            String id = StringKit.getUUID();
            boolean child = false;
            sqlEngine addExecute = new sqlEngine().execute("cr_engineexecute", "")
                    .addData("@id", id)
                    .addData("@eid", isChild ? initData.getChildId() : initData.getEngineId())
                    .addData("@state", "1")
                    .addData("@version", initData.getVersion() + "")
                    .addData("@sorts", isChild ? initData.getChildSort() + "" : initData.getSort() + "");
            LinkedHashMap<String, Object> params = StringKit.parseLinkedMap(data);
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
