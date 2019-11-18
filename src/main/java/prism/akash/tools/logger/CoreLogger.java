package prism.akash.tools.logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.engineEnum.conditionType;
import prism.akash.container.sqlEngine.engineEnum.groupType;
import prism.akash.container.sqlEngine.engineEnum.queryType;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.tools.date.dateParse;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 核心日志记录
 */
@Component
public class CoreLogger {

    @Autowired
    BaseApi baseApi;

    @Autowired
    dateParse dateParse;

    /**
     * 根据表Code获取对应Id值
     * @param tCode
     * @return
     */
    public String getTableByCode(String tCode) {
        List<BaseData> getTable = baseApi.selectBase(
                new sqlEngine().execute("cr_tables", "t")
                        .queryBuild(queryType.and, "t", "@code", conditionType.EQ, groupType.DEF, tCode)
                        .queryBuild(queryType.and, "t", "@state", conditionType.EQ, groupType.DEF, "1")
                        .selectFin(""));
        return getTable.size() > 0 ? getTable.get(0).getString("id") : null;
    }

    /**
     * 日志写入
     * @param type      变更类型（0-表/1-字段）
     * @param tCode     变更的表Code
     * @param reson     变更原因（0-新增/1-移除/2-更新）
     * @param dataId    执行变更后的数据编号
     * @param sourceDataId  源数据编号
     * @return
     */
    public int reCordLogger(String type, String tCode, String reson, String dataId, String sourceDataId) {
        String tid = getTableByCode(tCode);
        if (tid != null) {
            //新增日志
            sqlEngine addLog = new sqlEngine().execute("cr_logger", "l")
                    .addData("@id", UUID.randomUUID().toString().replaceAll("-", ""))
                    .addData("@type", type)
                    .addData("@tid", tid)
                    .addData("@executorId", "system_akash")
                    .addData("@reson", reson)
                    .addData("@updateTime", dateParse.formatDate("yyyy-MM-dd HH:mm:ss", new Date()))
                    .addData("@dataId", dataId)
                    .addData("@sourceDataId", sourceDataId)
                    .insertFin("");

            return baseApi.execute(addLog);
        }
        return 0;
    }
}
