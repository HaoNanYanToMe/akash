package prism.akash.api;

import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.sqlEngine;

import java.util.List;
import java.util.Map;

public interface BaseApi {

    /**
     * 数据查询
     * @param sqlEngine
     * @return
     */
    List<BaseData> selectBase(sqlEngine sqlEngine);

    /**
     * 数据总条数查询
     * @param sqlEngine
     * @return
     */
    int selectNums(sqlEngine sqlEngine);

    /**
     * 执行增删改操作
     * @param sqlEngine
     * @return
     */
    int executeBase(sqlEngine sqlEngine);
}
