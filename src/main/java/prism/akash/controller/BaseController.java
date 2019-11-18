package prism.akash.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import gui.ava.html.image.generator.HtmlImageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.extend.BaseDataExtends;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.tools.analysis.HtmlTemplateAnalysis;
import prism.akash.tools.file.FileHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Controller
public class BaseController extends BaseDataExtends{

    @Autowired
    BaseApi baseApi;

    @Autowired
    FileHandler fileHandler;

    @Autowired
    HtmlTemplateAnalysis htmlTemplateAnalysis;

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
}
