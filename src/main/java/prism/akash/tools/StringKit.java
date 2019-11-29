package prism.akash.tools;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.util.LinkedHashMap;
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

}
