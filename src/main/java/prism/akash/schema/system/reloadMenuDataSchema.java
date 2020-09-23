package prism.akash.schema.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prism.akash.container.BaseData;
import prism.akash.schema.BaseSchema;
import prism.akash.schema.login.accessLoginSchema;
import prism.akash.tools.StringKit;

import java.util.Map;


/**
 * 系统·重新加载权限数据
 * TODO : 系统·核心逻辑 （仅对Schema层有效，不对外部开放）
 *
 * @author HaoNan Yan
 */
@Service("menuSchema")
@Transactional(readOnly = true)
public class reloadMenuDataSchema extends BaseSchema {

    @Autowired
    accessLoginSchema accessLoginSchema;

    /**
     * 权限重载
     *
     * @param executeData {
     *                    *session_uid:登录后用户的id
     *                    *session_rid:登录后用户的role_id
     *                    }
     * @return 重载结果 true/false
     */
    public boolean reloadLoginData(BaseData executeData) {
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        //1.清除缓存
        redisTool.delete("login:role_data:id:" + data.getString("session_rid"));
        //2.重载缓存
        BaseData reload = new BaseData();
        reload.put("id", data.getString("session_uid"));
        reload.put("rid", data.getString("session_rid"));
        Map<String, Object> result = accessLoginSchema.accessLogin(pottingData("", reload));
        return (boolean) result.get("isLogin");
    }
}
