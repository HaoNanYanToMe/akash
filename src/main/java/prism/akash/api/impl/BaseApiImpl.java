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
import prism.akash.tools.date.dateParse;
import prism.akash.tools.logger.CoreLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component("baseApiImpl")
public class BaseApiImpl extends BaseDataExtends implements BaseApi {

    @Autowired
    BaseInteraction baseInteraction;
    @Autowired
    dateParse dateParse;
    @Autowired
    CoreLogger coreLogger;

    private BaseData getEngineData(String id, String executeData){
        return super.invokeDataInteraction(
                new sqlEngine(executeData),
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
    public BaseData selectByOne(String id, String executeData) {
        LinkedHashMap<String, Object> params = StringKit.parseLinkedMap(executeData);
        String tableCode = getTableCode(id);
        if(!tableCode.isEmpty()){
            BaseData select = new BaseData();
            StringBuffer sb = new StringBuffer();
            String fields = params.get("fields") == null ? "" : params.get("fields")+"";
            //如果有传入字段，则需进行字段对比，确认字段存在
            if(!fields.isEmpty()){
                //获取字段集合
                List<BaseData> fieldList = getFieldList(id);
                for (String f : fields.split(",")){
                    String [] fk = f.split("#");
                    for (BaseData field:fieldList) {
                        if(f.indexOf("#") > -1){
                            //TODO 存在别名
                            if(field.getString("code").equals(fk[0])){
                                sb.append(fk[0]).append(" as ").append(fk[1]).append(",");
                                //为了提升系统性能，一旦获取匹配值则跳出当前循环
                                break;
                            }
                        }else{
                            if(field.getString("code").equals(f)){
                                sb.append(f).append(",");
                                //为了提升系统性能，一旦获取匹配值则跳出当前循环
                                break;
                            }
                        }
                    }
                }
            }else{
                sb.append(" * ");
            }
            select.put("select","select " + sb.deleteCharAt(sb.length()-1) + " from " + tableCode + " where state = 0 and id = '" + params.get("id") + "'");
            List<BaseData> dataList = baseInteraction.select(select);
            return dataList.size() > 0 ? dataList.get(0) : null;
        }else{
            return null;
        }
    }

    @Override
    @Transactional
    public int execute(sqlEngine sqlEngine) {
        return baseInteraction.execute(sqlEngine.parseSql());
    }


    /**
     * 内部方法：根据指定数据表ID获取CODE
     * @param id    数据表ID
     * @return
     */
    private String getTableCode(String id){
        BaseData select = new BaseData();
        select.put("select","select code from cr_tables where state = 1 and id = '" + id + "'");
        List<BaseData> tables = baseInteraction.select(select);
        return tables.size() > 0 ? tables.get(0).get("code") + "" : "";
    }

    /**
     * 内部方法：根据指定数据表ID获取字段
     * @param id
     * @return
     */
    private List<BaseData> getFieldList(String id){
        BaseData select = new BaseData();
        select.put("select","select code from cr_field where state = 1 and tid = '" + id + "'");
        List<BaseData> fields = baseInteraction.select(select);
        return fields.size() > 0 ? fields :new ArrayList<>();
    }

    /**
     * 内部方法：根据ID获取指定数据
     * @param id        数据id
     * @param code      数据表CODE
     * @return
     *          -1 : 失败
     *          >-1：成功
     */
    private int getDataVersion(String id,String code){
        BaseData select = new BaseData();
        select.put("select","select version from " + code + " where state = 0 and id = '" + id + "'");
        List<BaseData> fields = baseInteraction.select(select);
        return fields.size() > 0 ? Integer.parseInt(fields.get(0).get("version")+"") : -1;
    }

    @Override
    @Transactional
    public String  insertData(String id, String executeData) {
        LinkedHashMap<String, Object> params = StringKit.parseLinkedMap(executeData);
        String tableCode = getTableCode(id);
        if(!tableCode.isEmpty()){
            //声明sql组装
            StringBuffer insert = new StringBuffer(" insert into ");
            insert.append(tableCode).append(" ( ");

            StringBuffer keys = new StringBuffer();
            StringBuffer values = new StringBuffer();
            //获取字段集合
            List<BaseData> fields = getFieldList(id);
            //新增的UUID
            String uuid = StringKit.getUUID();
            if (fields.size() > 0){
                //主键UUID
                params.put("id", uuid);
                //TODO 核心表没有create_time字段
                if (!tableCode.split("_")[0].equals("cr")){
                    //数据创建时间
                    params.put("create_time", dateParse.formatDate("yyyy-MM-dd HH:mm:ss", new Date()));
                }
                //数据版本
                params.put("version", 0);
                //数据状态(1-正常，0-已删除）
                params.put("state", 0);
                for (String key : params.keySet()) {
                    //数据强校验，以保证传入的数据字段真实有效
                    for (BaseData field:fields) {
                        if(field.getString("code").equals(key)){
                            keys.append(key).append(",");
                            values.append("'").append(params.get(key)).append("',");
                            //为了提升系统性能，一旦获取匹配值则跳出当前循环
                            break;
                        }
                    }
                }
                insert.append(keys.deleteCharAt(keys.length()-1)).append(" ) values ( ");
                insert.append(values.deleteCharAt(values.length()-1)).append(" )");

                //执行新增
                BaseData bd = new BaseData();
                bd.put("executeSql",insert.toString());
                return baseInteraction.execute(bd) > 0 ? uuid : "0";
            }else{
                return "-1";
            }
        }else{
            return "-2";
        }
    }

    @Override
    public int updateData(String id, String executeData) {
        //数据更新状态
        int state = 0;
        LinkedHashMap<String, Object> params = StringKit.parseLinkedMap(executeData);
        String tableCode = getTableCode(id);
        if(!tableCode.isEmpty()){
            //获取当前数据版本
            int version = getDataVersion(params.get("id")+"",tableCode);
            int updVersion = params.get("version") == null ? -1 : Integer.parseInt(params.get("version") + "");
            //判断当前数据是否允许更新
            if(version == -1){
                state = -9;
            }else if(updVersion == -1){
                state = -8;
            }else if(updVersion > version){
                //TODO 当待更新版本号大于系统版本号时，允许更新
                //获取字段集合
                List<BaseData> fields = getFieldList(id);
                if(fields.size() > 0){
                    StringBuffer update = new StringBuffer(" update  ");
                    update.append(tableCode).append(" set ");
                    for (String key : params.keySet()) {
                        //数据强校验，以保证传入的数据字段真实有效
                        for (BaseData field:fields) {
                            if(field.getString("code").equals(key)){
                                update.append(key).append(" = ").append(params.get(key)).append(" , ");
                                //为了提升系统性能，一旦获取匹配值则跳出当前循环
                                break;
                            }
                            if(field.get("id") != null){
                                if(field.getString("id").equals(key)){
                                    //禁止通过传参方式更新数据的主键uuid及数据版本号
                                    break;
                                }
                            }
                            if(field.get("version") != null){
                                if(field.getString("version").equals(key)){
                                    //禁止通过传参方式更新数据的主键uuid及数据版本号
                                    break;
                                }
                            }
                        }
                        update.append(" version = ").append(updVersion);
                        if (!tableCode.split("_")[0].equals("cr")){
                            //数据最后访问时间
                            update.append(" ,last_time = '").append(dateParse.formatDate("yyyy-MM-dd HH:mm:ss", new Date())).append("'");
                        }
                        update.append(" where id = '").append(params.get("id")).append("'");
                        //TODO sys系统源数据在更新时需要追加is_lock条件
                        if (tableCode.split("_")[0].equals("sys")){
                            update.append(" and is_lock = 0 ");
                        }
                        //执行新增
                        BaseData bd = new BaseData();
                        bd.put("executeSql",update.toString());
                        return baseInteraction.execute(bd);
                    }
                }else{
                    state = -1;
                }
            }else{
                state = -3;
            }
        }else{
            state = -2;
        }
        return state;
    }

    @Override
    public int deleteData(String id, String executeData) {
        //删除状态
        int state = 0;
        LinkedHashMap<String, Object> params = StringKit.parseLinkedMap(executeData);
        String tableCode = getTableCode(id);
        if(!tableCode.isEmpty()){
            if(params.get("id") != null){
                if(!(params.get("id")+"").isEmpty()){
                    BaseData bd = new BaseData();
                    String delete = "delete from "+tableCode+" where id = " + id;
                    //TODO system系统源数据在删除时需要追加is_lock条件
                    if (tableCode.split("_")[0].equals("sys")){
                        delete  +=  " and is_lock = 0 ";
                    }
                    bd.put("executeSql", delete);
                    state = baseInteraction.execute(bd);
                }
            }else{
                return -1;
            }
        }else{
            return -2;
        }
        return state;
    }

    @Override
    @Transactional
    public int insertInitData(String table, String executeData) {
        int suc = 0;
        String tid = StringKit.getUUID();
        BaseData insertTable = new BaseData();
        BaseData updateTable = new BaseData();
        String[] tables = table.split("#");
        //TODO: 获取最新的数据版本号
        int version = 0;
        //TODO: 获取系统内已有表信息
        List<BaseData> tableList = this.selectBase(new sqlEngine()
                .execute("cr_tables", "t")
                .queryBuild(queryType.and, "t", "@code", conditionType.EQ, groupType.DEF, tables[0])
                .selectFin(""));
        if(tableList.size() > 0){
            tid = tableList.get(0).getString("id");
            version = Integer.parseInt(tableList.get(0).get("version")+"") + 1;
            //TODO: 执行数据更新
            updateTable.put("executeSql", "update cr_tables set code = '" + tables[0] + "',name = '" + tables[1] + "',version = '" + version + "' where id = '" + tid + "'");
            suc = baseInteraction.execute(updateTable);
        }else{
            insertTable.put("executeSql",
                    "INSERT INTO cr_tables (id,code,name,state,version) VALUES ('" + tid + "','" + tables[0] + "','" + tables[1] + "',1,'" + version + "')");
            suc = baseInteraction.execute(insertTable);
        }
        if (suc == 1) {
            //TODO: 新增或更新表成功后执行
            coreLogger.reCordLogger("0", tables[0], tableList.size() > 0 ? "2" : "0", tid, version + "");

            //TODO: 同步字段表数据时，会批量更新先前数据状态为禁用（误操作保护）后重新提交创建
            baseInteraction.execute(new sqlEngine()
                    .execute("cr_field", "c")
                    .updateData("@state", "0")
                    .queryBuild(queryType.and, "c", "@tid", conditionType.EQ, groupType.DEF, tid)
                    .updateFin("").parseSql());

            //TODO: 新增表成功
            LinkedHashMap<String, Object> params = JSONObject.parseObject(executeData, new TypeReference<LinkedHashMap<String, Object>>() {
            });

            //TODO: 字段迁移
            List<BaseData> fetch = new ArrayList<>();
            String keys = "id,code,name,tid,type,size,sorts,state,version";
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
                fe.put("version", version);
                fe.put("sorts", sorts);
                fetch.add(fe);
                sorts++;
            }
            //TODO: 执行新增字段
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
