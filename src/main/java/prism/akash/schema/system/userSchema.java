package prism.akash.schema.system;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.schema.BaseSchema;

import java.util.List;
import java.util.Map;

/**
 * 系统用户相关逻辑类
 *       TODO : 系统·核心逻辑 （独立）
 * @author HaoNan Yan
 */
@Service("userSchema")
@Transactional(readOnly = true)
public class userSchema extends BaseSchema {

    /**
     * 通过企邮方式进行鉴权
     * TODO 需要先通过集团U2或A2认证
     *
     * @param email
     * @return
     */
    public Map<String,String> authentication(String email) {
        //判断当前用户是否具备授权条件 ： email符合匹配、且账号状态为正常（ 0 ）
        String access = "select id,email,name,type from sys_user where email = '" + email + "' and state = 0 ";
        List<BaseData> users = baseApi.selectBase(new sqlEngine().setSelect(access));
        //获取当前用户信息
        BaseData user = users.size() > 0 ? users.get(0) : null;
        if (user != null) {

        }
        return null;
    }

}
