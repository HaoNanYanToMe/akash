package prism.akash.controller.proxy;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import prism.akash.container.BaseData;
import prism.akash.tools.cache.CacheClass;
import prism.akash.tools.context.SpringContextUtil;
import prism.akash.tools.reids.RedisTool;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * 系统核心及拓展逻辑Schema代理类
 *       TODO : 系统·核心逻辑 / 拓展业务逻辑代理
 * @author HaoNan Yan
 */
@Component
public class BaseProxy implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(BaseProxy.class);

    @Autowired
    RedisTool redisTool;

    /**
     * 执行方法
     * @param schemaName    需要反射代理的Schema（系统固有逻辑）对象
     * @param methodName    需要代理执行调用的方法名称
     * @param id            数据表id /  sql数据引擎id
     * @param executeData   代理入参数据对象
     * @return
     */
    public Object invokeMethod(String schemaName, String methodName, String id, BaseData executeData) {
        //通过反射代理的class对象
        Class clazz;
        //执行invoke后返回的数据对象
        Object reObject = null;
        //需要invoke执行调用的方法
        Method m1;
        //通过反射的schemaClassObject
        Object obj = proxySchema(schemaName);
        clazz = obj.getClass();
        try {
            //执行方法
            m1 = clazz.getDeclaredMethod(methodName, BaseData.class);
            //封装执行参数
            BaseData execute = new BaseData();
            execute.put("id", id);
            execute.put("executeData", JSON.toJSONString(executeData));
            //执行操作
            reObject = m1.invoke(obj, execute);
        } catch (NoSuchMethodException e) {
            logger.error("BaseProxy:invokeMethod:NoSuchMethodException -> " + schemaName + " - " + methodName + " is not Found");
        } catch (IllegalAccessException e) {
            logger.error("BaseProxy:invokeMethod:IllegalAccessException -> " + schemaName + " - " + methodName + " is no Access");
        } catch (InvocationTargetException e) {
            logger.error("BaseProxy:invokeMethod:IllegalAccessException -> " + schemaName + " - " + methodName + " undefined error : " + e.getMessage() + " / " + e.getCause().getMessage());
        }
        return reObject;
    }

    /**
     * 获取代理的Schema对象
     *
     * @param schemaName schema名称 ： 如未填写默认使用base
     * @return
     */
    private Object proxySchema(String schemaName) {
        Object sObj = null;
        //如果SchemaName为空,则默认使用baseSchema
        schemaName = schemaName.isEmpty() || schemaName == null ? "base" : schemaName;
        String schema = schemaName + "Schema";
        //从cache中获取Schema对象
        Object schemaObj = CacheClass.getCache("schema:proxy:" + schemaName);
        if (schemaObj != null) {
            //将cache中获取的数据转换成实际存在的schema对象
            sObj = schemaObj;
        } else {
            //若cache中未获取到schema对象则进行初始化
            Class<?> cls = null;
            try {
                cls = Class.forName("prism.akash.schema.BaseSchema");
                sObj = SpringContextUtil.getBean(schema, cls);
                //持久化当前schema对象
                CacheClass.setCache("schema:proxy:" + schemaName , sObj , -1);
            } catch (ClassNotFoundException e) {
                logger.error("BaseConfig:proxySchema:ClassNotFoundException -> " + schema + " is not Found");
            }
        }
        return sObj;
    }
}
