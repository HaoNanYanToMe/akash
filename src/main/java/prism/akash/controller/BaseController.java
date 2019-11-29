package prism.akash.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.converter.ConverterData;
import prism.akash.container.converter.sqlConverter;
import prism.akash.container.extend.BaseDataExtends;
import prism.akash.tools.StringKit;
import prism.akash.tools.file.FileHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BaseController extends BaseDataExtends{

    @Autowired
    BaseApi baseApi;

    @Autowired
    FileHandler fileHandler;

    @Autowired
    sqlConverter sqlConverter;
    /**
     * 查询全部信息（含分页）
     * @param eid
     * @param data
     * @return
     */
    @RequestMapping(value = "/selectPage",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public String selectPage(String eid,String data){
        return JSON.toJSONString(baseApi.selectPage(eid,data));
    }

    /**
     * 查询全部信息
     * @param eid
     * @param data
     * @return
     */
    @RequestMapping(value = "/select",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public String select(String eid,String data){
        return JSON.toJSONString(baseApi.select(eid,data));
    }

    /**
     * 数据变更
     * @param eid
     * @param data
     * @return
     */
    @RequestMapping(value = "/executeBase",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public String executeBase(String eid,String data){
        return JSON.toJSONString(baseApi.execute(eid,data));
    }


    /**
     * 新增（基础）
     * @param id    表对应的ID
     * @param data  表内字段
     * @return
     */
    @RequestMapping(value = "/insertBase",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public String insertBase(String id,String data){
        return JSON.toJSONString(baseApi.insertData(id,data));
    }

    /**
     * 手动初始化基本数据信息
     * @param table
     * @param data
     * @return
     */
    @RequestMapping(value = "/initData",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public String initData(String table,String data){
        return JSON.toJSONString(baseApi.insertInitData(table,data));
    }

    /**
     * 获取图片及文件流
     * @param response
     * @param fileName
     * @throws IOException
     */
    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping(value = "/getFile",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public void getFile(
            HttpServletResponse response,
            @RequestParam(value = "fileName",required = false) String fileName
    ) throws IOException {
        fileHandler.getFile(response,fileName);
    }

    @CrossOrigin(origins = "*", maxAge = 3600)
    @ResponseBody
    @RequestMapping(value = "/testAddEngine",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public void testAddEngine(){
        String i = StringKit.getUUID();

        List<BaseData> list = new ArrayList<>();

        BaseData o1 = new BaseData();
        o1.put("executeTag", "execute");

        BaseData e1 = new BaseData();
        e1.put("tableName", "test");
        e1.put("alias", "t");
        o1.put("executeData", JSON.toJSONString(e1));

        list.add(o1);


        List<BaseData> list2 = new ArrayList<>();

        BaseData z1 = new BaseData();
        z1.put("executeTag", "execute");

        BaseData zd1 = new BaseData();
        zd1.put("alias", "t");
        zd1.put("tableName", "tables");

        z1.put("executeData", JSON.toJSONString(zd1));

        BaseData z3 = new BaseData();
        z3.put("queryType", "and");
        z3.put("table", "t" );
        z3.put("key", "id");
        z3.put("conditionType", "EQ");
        z3.put("exQueryType", "DEF");
        z3.put("value", "testKey#testKey参数");


        BaseData zd3 = new BaseData();
        zd3.put("executeTag", "queryBuild");

        zd3.put("executeData", JSON.toJSONString(z3));
        BaseData z4 = new BaseData();
        z4.put("executeTag", "selectFin");
        list2.add(z1);
        list2.add(zd3);
        list2.add(z4);

        BaseData oo = new BaseData();
        oo.put("childList", JSON.toJSONString(list2));
        oo.put("executeTag", "executeChild");


        BaseData zz1 = new BaseData();
        zz1.put("alias", "t");
        oo.put("executeData", JSON.toJSONString(zz1));
        list.add(oo);


        BaseData o2 = new BaseData();
        o2.put("executeTag", "appointColumn");

        BaseData e2 = new BaseData();
        e2.put("appointTable", "t");
        e2.put("appointColumns", "id,executetag");
        o2.put("executeData", JSON.toJSONString(e2));
        list.add(o2);


        BaseData o3 = new BaseData();
        o3.put("executeTag", "queryBuild");

        BaseData e3 = new BaseData();
        e3.put("queryType", "and");
        e3.put("table", "c" );
        e3.put("key", "id");
        e3.put("conditionType", "EQ");
        e3.put("exQueryType", "DEF");
        e3.put("value", "value#value参数");
        o3.put("executeData", JSON.toJSONString(e3));
        list.add(o3);

        BaseData o4 = new BaseData();
        o4.put("executeTag", "selectFin");
        list.add(o4);

        System.out.println(JSON.toJSONString(sqlConverter.createBuild("测试引擎_acc" , "t_Acc" , "测试引擎_acc",JSON.toJSONString(list))));
    }
}
