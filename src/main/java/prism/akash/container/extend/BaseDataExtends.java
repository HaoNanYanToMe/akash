package prism.akash.container.extend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.engineEnum.*;
import prism.akash.container.sqlEngine.sqlEngine;

import java.io.Serializable;
import java.util.List;

/**
 * 基础数据拓展棱镜
 */
public class BaseDataExtends implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(BaseDataExtends.class);

    @Autowired
    BaseApi baseApi;

    /**
     * 动态SQL构造器
     *
     * @param sqlEngine SQL引擎对象
     * @param engineId  需要动态获取的数据引擎编号
     * @param data      入参参数
     * @return
     */
    public sqlEngine invokeDataInteraction(sqlEngine sqlEngine, String engineId, String data, boolean isChild) {
        long startTime = System.currentTimeMillis();
        //对engineId进行格式化
        engineId = engineId == null ? "" : engineId;
        if (!engineId.trim().equals("")) {
            BaseData sel = new BaseData();
            sel.put("eid", engineId);
            sel.put("type", "0");
            //TODO: 判断当前是否为嵌套子查询
            List<BaseData> engineFlow = baseApi.selectBase(isChild ?
                    new sqlEngine()
                            .execute("engineexecute", "e")
                            .queryBuild(queryType.and, "e", "eid", conditionType.EQ, null,"eid")
                            .queryBuild(queryType.and, "e", "type", conditionType.EQ, null,"type")
                            .dataSort("e", "sorts", sortType.ASC)
                            .selectFin(JSON.toJSONString(sel))
                    :
                    new sqlEngine()
                            .execute("engineexecute", "e")
                            .joinBuild("engineList", "en", joinType.L).joinColunm("e", "eid", "id").joinFin()
                            .queryBuild(queryType.and, "e", "eid", conditionType.EQ, null,"eid")
                            .queryBuild(queryType.and, "en", "type", conditionType.EQ, null,"type")
                            .queryBuild(queryType.and, "e", "type", conditionType.EQ, null,"type")
                            .dataSort("e", "sorts", sortType.ASC)
                            .selectFin(JSON.toJSONString(sel)));
            if (engineFlow.size() > 0) {
                for (BaseData bd : engineFlow) {
                    try {
                        //TODO: 操作标识非空判定
                        if (bd.get("executeTag") != null) {
                            String executeTag = bd.getString("executeTag");
                            //TODO: 操作标识句柄结束返回判定
                            if (executeTag.equals("selectFin")) {
                                sqlEngine.selectFin(data);
                            } else if (executeTag.equals("joinFin")) {
                                sqlEngine.joinFin();
                            } else if (executeTag.equals("insertFin")) {
                                sqlEngine.insertFin(data);
                            } else {
                                JSONObject jo = JSONObject.parseObject(bd.get("executeData") + "");
                                switch (executeTag) {
                                    case "execute":
                                        sqlEngine.execute(jo.get("tableName") + "", jo.get("alias") + "");
                                        break;
                                    case "executeChild":
                                        sqlEngine.executeChild(this.invokeDataInteraction(new sqlEngine(), bd.getString("id"), data, true)
                                                , jo.get("alias") + "");
                                        break;
                                    case "joinWhere":
                                        sqlEngine.joinWhere(queryType.getQueryType(jo.getString("queryType"))
                                                , jo.get("key") + "",
                                                conditionType.getconditionType(jo.getString("conditionType")),
                                                jo.get("value") + "");
                                        break;
                                    case "joinBuild":
                                        sqlEngine.joinBuild(jo.get("joinTable") + "", jo.get("joinAlias") + "", joinType.getJoinType(jo.getString("joinType")));
                                        break;
                                    case "joinChildBuild":
                                        sqlEngine.joinChildBuild(this.invokeDataInteraction(new sqlEngine(),
                                                bd.getString("id"), data, true), jo.get("joinAlias") + "",
                                                joinType.getJoinType(jo.getString("joinType")));
                                        break;
                                    case "joinColunm":
                                        sqlEngine.joinColunm(jo.get("joinTable") + "", jo.get("joinFrom") + "", jo.get("joinTo") + "");
                                        break;
                                    case "dataPaging":
                                        sqlEngine.dataPaging(jo.get("pageNo") + ""
                                                , jo.get("pageSize") + "");
                                        break;
                                    case "dataSort":
                                        sqlEngine.dataSort(jo.get("table") + "", jo.get("key") + "", sortType.getSortType(jo.getString("sortType")));
                                        break;
                                    case "caseBuild":
                                        sqlEngine.caseBuild(jo.get("caseAlias") + "");
                                        break;
                                    case "caseWhenQuery":
                                        sqlEngine.caseWhenQuery(queryType.getQueryType(jo.getString("whenQuery"))
                                                , jo.get("whenTable") + "", jo.get("whenColumn") + ""
                                                , conditionType.getconditionType(jo.getString("whenCondition"))
                                                , groupType.getgroupType(jo.getString("exCaseType"))
                                                , jo.get("whenValue") + "");
                                        break;
                                    case "caseThen":
                                        sqlEngine.caseThen(jo.get("thenValue") + "");
                                        break;
                                    case "caseFin":
                                        sqlEngine.caseFin(jo.get("elseValue") + "");
                                        break;
                                    case "appointColumn":
                                        sqlEngine.appointColumn(jo.get("appointTable") + ""
                                                , groupType.getgroupType(jo.getString("exAppointType")),
                                                jo.get("appointColumns") + "");
                                        break;
                                    case "groupBuild":
                                        sqlEngine.groupBuild(jo.get("groupTable") + "", jo.get("groupColumns") + "");
                                        break;
                                    case "groupColumn":
                                        sqlEngine.groupColumn(groupType.getgroupType(jo.getString("groupType"))
                                                , jo.get("groupTable") + ""
                                                , jo.get("groupColumns") + "");
                                        break;
                                    case "groupHaving":
                                        sqlEngine.groupHaving(groupType.getgroupType(jo.getString("groupType"))
                                                , queryType.getQueryType(jo.getString("queryType"))
                                                , jo.get("groupTable") + "", jo.get("groupColumn") + ""
                                                , conditionType.getconditionType(jo.getString("conditionType"))
                                                , jo.get("value") + "");
                                        break;
                                    case "groupHavingChild":
                                        sqlEngine.groupHavingChild(groupType.getgroupType(jo.getString("groupType"))
                                                , queryType.getQueryType(jo.getString("queryType"))
                                                , jo.get("groupTable") + "", jo.get("groupColumn") + ""
                                                , conditionType.getconditionType(jo.getString("conditionType"))
                                                , this.invokeDataInteraction(new sqlEngine(), bd.getString("id"), data, true));
                                        break;
                                    case "queryBuild":
                                        sqlEngine.queryBuild(queryType.getQueryType(jo.getString("queryType"))
                                                , jo.get("table") + "", jo.get("key") + ""
                                                , conditionType.getconditionType(jo.getString("conditionType"))
                                                , groupType.getgroupType(jo.getString("exQueryType"))
                                                , jo.get("value") + "");
                                        break;
                                    case "queryChild":
                                        sqlEngine.queryChild(queryType.getQueryType(jo.getString("queryType"))
                                                , jo.get("table") + "", jo.get("key") + ""
                                                , conditionType.getconditionType(jo.getString("conditionType"))
                                                , this.invokeDataInteraction(new sqlEngine(), bd.getString("id"), data, true));
                                        break;
                                    case "addData":
                                        sqlEngine.addData(jo.get("addkey") + "",
                                                jo.get("value") + "");
                                        break;
                                    case "insertCopy":
                                        sqlEngine.insertCopy(this.invokeDataInteraction(new sqlEngine(),bd.getString("id"),data,true));
                                        break;
                                    case "insertFetchPush":
                                        sqlEngine.insertFetchPush(data,jo.get("keys") + "");
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        logger.error("executeData:数据对象转储出现错误 -> (" + e.getMessage() + ")");
                    }
                }
            }
        }
        long endTime = System.currentTimeMillis();
        logger.info("sql生成耗时:" + (endTime - startTime));
        return sqlEngine;
    }
}
