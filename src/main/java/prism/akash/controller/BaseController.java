package prism.akash.controller;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import prism.akash.container.BaseData;
import prism.akash.controller.proxy.BaseProxy;

import java.io.Serializable;

/**
 * 通用接口
 * TODO : 系统·通用接口
 *
 * @author HaoNan Yan
 */
@RestController
public class BaseController extends BaseProxy implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(BaseController.class);

    /**
     *
     * @param schemaName
     * @param methodName
     * @param data
     * @return
     */
    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping(value = "/executeUnify",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public String executeUnify(
            @RequestParam(value = "schemaName") String schemaName,
            @RequestParam(value = "methodName") String methodName,
            @RequestParam(value = "data", required = false, defaultValue = "{}") String data) {
        BaseData execute = JSON.parseObject(data, BaseData.class);
        return JSON.toJSONString(invokeMethod(schemaName, methodName, execute));
    }


    /**
     * 手动初始化基本数据信息
     * @param table
     * @param data
     * @return
     */
//    @RequestMapping(value = "/initData",
//            method = RequestMethod.POST,
//            produces = "application/json;charset=UTF-8")
//    public String initData(String table, String data) {
//        return JSON.toJSONString(baseApi.insertInitData(table,data));
//    }


}
