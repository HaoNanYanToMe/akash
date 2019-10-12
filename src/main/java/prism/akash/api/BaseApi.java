package prism.akash.api;

import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.sqlEngine;

import java.util.List;

public interface BaseApi {

    List<BaseData> selectBase(sqlEngine sqlEngine);

}
