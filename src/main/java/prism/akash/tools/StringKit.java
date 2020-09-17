package prism.akash.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringKit {


    /**
     * 判断Key或value中是否含有特殊字符
     * TODO ：主要提供给sqlEngine校验使用,仅允许及开放使用下划线_
     * @param str
     * @return true为包含，false为不包含
     */
    public static boolean isSpecialChar(String str) {
        String regEx = "[ `~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        return isSpecial(regEx,str);
    }

    /**
     * 判断Column字段是否含有非法字符
     * TODO ：主要提供给sqlStepBuild校验使用,仅允许及开放使用:_#,@
     * @param str true为包含，false为不包含
     * @return
     */
    public static boolean isSpecialColumn(String str) {
        String regEx = "[ `~!$%^&*()+=|{}':;'\\[\\].<>/?~！￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        return isSpecial(regEx,str);
    }

    /**
     * 通用代码提取
     * @param regEx
     * @param str
     * @return
     */
    private static boolean isSpecial(String regEx,String str) {
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
     * @param data     待处理的JSON字符串
     * @return
     */
    public static LinkedHashMap<String, Object> parseLinkedMap(String data){
        return JSONObject.parseObject(data, new TypeReference<LinkedHashMap<String, Object>>() {
        });
    }

    /**
     * 判断一个字符串是否符合JSON格式标准
     *
     * @param content
     * @return
     */
    public static boolean isJSON(String content) {
        try {
            JSONObject jsonStr = JSONObject.parseObject(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断一个字符串是否符合JSONArray格式标准
     *
     * @param content
     * @return
     */
    public static boolean isJSONArray(String content) {
        try {
            JSONArray jsonArray = JSONArray.parseArray(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将对象转储为标准数据
     *
     * @param data 待转储的数据对象
     * @return
     */
    public static String formateSchemaData(Object data) {
        //TODO 判断当前数据是否为JSON格式
        String content = String.valueOf(data);
        boolean existJson = isJSON(content) || isJSONArray(content);
        Map<String, Object> result = new HashMap<>();
        result.put("result", existJson ? !content.equals("null") ? "1" : "-8" : content);
        result.put("resultData", existJson ? content : formateType(content));
        return JSON.toJSONString(result);
    }

    /**
     * 标准数据备注转换
     *
     * @param type
     * @return
     */
    private static String formateType(String type) {
        return type.equals("-1") ? "操作失败：参数字段不匹配（可操作字段不存在或「id」参数未填写）" :
                type.equals("-2") ? "操作失败：数据表不存在！" :
                        type.equals("-3") ? "操作失败：数据版本不匹配（当前数据已「过期」，请重试）" :
                                type.equals("0") ? "操作失败：执行异常，请联系管理员" :
                                        type.equals("-8") || type.equals("") ? "操作失败：待操作数据不存在" :
                                                type.equals("-9") ? "操作失败：参数字段不匹配（「version」参数未填写）" :
                                                        type.equals("1") ? "操作成功" :
                                                                type.length() == 32 ? "操作成功" : "操作失败：未知错误，请联系管理员";
    }
}
