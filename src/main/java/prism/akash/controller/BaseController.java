package prism.akash.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import prism.akash.api.BaseApi;
import prism.akash.container.extend.BaseDataExtends;
import prism.akash.container.sqlEngine.sqlEngine;

@Controller
public class BaseController extends BaseDataExtends{

    @Autowired
    BaseApi baseApi;

    @RequestMapping(value = "/selectBase",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public String selectBase(String eid,String data){
        sqlEngine reObj = super.invokeDataInteraction(new sqlEngine(),eid,data,false);
        return reObj == null ? "" : JSON.toJSONString(baseApi.selectBase(reObj));
    }
}
