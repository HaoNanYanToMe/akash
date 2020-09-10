package prism.akash.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.corba.se.spi.ior.ObjectKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.converter.ConverterData;
import prism.akash.container.converter.sqlConverter;
import prism.akash.container.extend.BaseDataExtends;
import prism.akash.tools.StringKit;
import prism.akash.tools.file.FileHandler;
import prism.akash.tools.file.FileUpload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class BaseController extends BaseDataExtends{

    @Autowired
    BaseApi baseApi;

    @Autowired
    FileHandler fileHandler;

    @Autowired
    FileUpload  fileUpload;

    @Autowired
    sqlConverter sqlConverter;

    /**
     * 查询全部信息（含分页）
     * @param eid
     * @param data
     * @return
     */
    @CrossOrigin(origins = "*", maxAge = 3600)
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
    @CrossOrigin(origins = "*", maxAge = 3600)
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
    @CrossOrigin(origins = "*", maxAge = 3600)
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



    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping(value = "/upFile",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String upLoad(
            HttpServletRequest request,
            @RequestParam(value = "file",required = false) MultipartFile[] file
    )throws Exception{
        String files = "";
        //文件处理
        String  ffile =new SimpleDateFormat("yyyyMMdd").format(new Date());
        if (null != file && file.length != 0) {
            files = fileHandler.uploadFiledList(file,request,"" + ffile);
        }
        return  files;
    }


    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping(value = "/upExcelQuery",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String upExcelQuery(
            HttpServletRequest request,
            @RequestParam(value = "file",required = false) MultipartFile[] file
    )throws Exception{
        BaseData bd = new BaseData();
        bd.put("execute","bs_dataq");

        String files = this.upLoad(request,file);
        JSONArray jo = JSONArray.parseArray(files);
        fileUpload.importExcel(fileHandler.defaultFilePath +  jo.get(0).toString(),bd,true,1000);
        return  "0";
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


    /**
     * 创建引擎（当前版本仅支持查询）
     * @param s   executeData  引擎流程数据
     * @param n   name         引擎名称
     * @param c   code         引擎Code（唯一码）
     * @param ne  note         引擎备注信息
     * @return
     */
    @CrossOrigin(origins = "*", maxAge = 3600)
    @ResponseBody
    @RequestMapping(value = "/testEngine",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public String testAddEngine(@Param("s") String s,
                                @Param("n") String n,
                                @Param("c") String c,
                                @Param("ne") String ne){
        return  JSON.toJSONString(sqlConverter.createBuild(n , c , ne,s));
    }
}
