package prism.akash.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import prism.akash.container.BaseData;
import prism.akash.api.BaseApi;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.dataInteraction.BaseInteraction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("baseApiImpl")
public class BaseApiImpl implements BaseApi{

    @Autowired
    BaseInteraction baseInteraction;

    @Override
    public List<BaseData> selectBase(sqlEngine sqlEngine) {
        return baseInteraction.select(sqlEngine.parseSql());
    }

    @Override
    public int selectNums(sqlEngine sqlEngine) {
        return baseInteraction.selectNums(sqlEngine.parseSql());
    }

    @Override
    public int executeBase(sqlEngine sqlEngine) {
        return baseInteraction.execute(sqlEngine.parseSql());
    }

}
