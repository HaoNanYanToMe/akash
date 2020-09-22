package prism.akash.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import prism.akash.container.BaseData;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统 字符串 / BaseData对象封装处理
 * TODO : 系统·字符串及BaseData对象处理
 *
 * @author HaoNan Yan
 */
public class StringKit {

    /**
     * 判断Key或value中是否含有特殊字符
     * TODO ：主要提供给sqlEngine校验使用,仅允许及开放使用下划线_
     *
     * @param str
     * @return true为包含，false为不包含
     */
    public static boolean isSpecialChar(String str) {
        String regEx = "[ `~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        return isSpecial(regEx, str);
    }

    /**
     * 判断Column字段是否含有非法字符
     * TODO ：主要提供给sqlStepBuild校验使用,仅允许及开放使用:_#,@
     *
     * @param str true为包含，false为不包含
     * @return
     */
    public static boolean isSpecialColumn(String str) {
        String regEx = "[ `~!$%^&*()+=|{}':;'\\[\\].<>/?~！￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        return isSpecial(regEx, str);
    }

    /**
     * 通用代码提取
     *
     * @param regEx
     * @param str
     * @return
     */
    private static boolean isSpecial(String regEx, String str) {
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }

    /**
     * TODO : 生成32位标准UUID
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 将json字符串转换为有序集合LinkedHashMap
     *
     * @param data 待处理的JSON字符串
     * @return
     */
    public static LinkedHashMap<String, Object> parseLinkedMap(String data) {
        return JSONObject.parseObject(data, new TypeReference<LinkedHashMap<String, Object>>() {
        });
    }

    /**
     * 将对象转储为标准数据
     *
     * @param data 待转储的数据对象
     * @return
     */
    public static String formateSchemaData(Object data) {
        String content = String.valueOf(data);
        //TODO 判断当前数据格式是否为基础格式 String int
        boolean existBase = data instanceof String || data instanceof Integer;
        Map<String, Object> result = new HashMap<>();
        result.put("result", !existBase ? !content.equals("null") ? "1" : "-8" : content.length() == 32 ? "1" : data);
        result.put("resultData", !existBase ? !content.equals("null") ? data : "操作失败：待操作数据不存在" : formateType(content));
        return JSON.toJSONString(result);
    }

    /**
     * 标准数据备注转换
     *
     * @param content
     * @return
     */
    private static String formateType(String content) {
        String result = "操作失败：未知错误，请联系管理员";

        if (content.length() == 32) {
            result = content;
        } else {
            switch (content) {
                case "1":
                    result = "操作成功";
                    break;
                case "-1":
                    result = "操作失败：参数字段不匹配（可操作字段不存在或「id」参数未填写）";
                    break;
                case "-2":
                    result = "操作失败：数据表不存在";
                    break;
                case "-3":
                    result = "操作失败：数据版本不匹配（当前数据已「过期」，请重试）";
                    break;
                case "0":
                    result = "操作失败：执行异常，请联系管理员";
                    break;
                case "-8":
                    result = "操作失败：待操作数据不存在";
                    break;
                case "-9":
                    result = "操作失败：参数字段不匹配（「version」参数未填写）";
                    break;
                case "-5":
                    result = "操作失败：缺少关键字段，请检查后重试";
                    break;
                case "":
                    result = "操作失败：待操作数据不存在";
                    break;
                case "UR1":
                    result = "操作失败：用户不存在";
                    break;
                case "UR2":
                    result = "操作失败：权限未定义";
                    break;
                case "UR3":
                    result = "操作失败：菜单未定义";
                    break;
                case "UR4":
                    result = "操作失败：数据源未定义";
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    /**
     * 将数据转化成BaseData对象
     * TODO Schema层使用
     *
     * @param data
     * @return
     */
    public static BaseData parseBaseData(String data) {
        BaseData execute = new BaseData();
        if (!data.isEmpty() && data != null) {
            execute = JSON.parseObject(data, BaseData.class);
        }
        return execute;
    }

    /**
     * 拓展Schema层入参数据解析
     * TODO Schema层使用
     * TODO 非基础（base）类Schema层使用Controller提供的数据时需要将proxy处理过的数据对象进行定向解析
     *
     * @param data
     * @return
     */
    public static String parseSchemaExecuteData(BaseData data) {
        if (data != null) {
            if (data.get("executeData") != null) {
                data = parseBaseData(data.getString("executeData"));
            } else {
                data = new BaseData();
            }
        } else {
            data = new BaseData();
        }
        return JSON.toJSONString(data);
    }
}
