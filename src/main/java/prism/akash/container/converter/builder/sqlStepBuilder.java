package prism.akash.container.converter.builder;

import prism.akash.container.BaseData;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO : 数据逻辑引擎信息整合构造器
 */
public class sqlStepBuilder {

    private List<BaseData> stepArray = new ArrayList<>();

    private BaseData step;

    private String  demand_execute;

    public sqlStepBuilder(String demand){
        demand_execute = demand;
    }

}
