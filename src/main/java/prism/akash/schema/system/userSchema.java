package prism.akash.schema.system;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.schema.BaseSchema;
import prism.akash.tools.StringKit;
import prism.akash.tools.annocation.Access;
import prism.akash.tools.annocation.Schema;
import prism.akash.tools.annocation.checked.AccessType;

import java.util.List;

/**
 * 系统用户相关逻辑类
 *       TODO : 系统·核心逻辑 （独立）
 * @author HaoNan Yan
 */
@Service
@Transactional(readOnly = true)
@Schema(code = "user", name = "系统用户")
public class userSchema extends BaseSchema {


    /**
     * 新增用户基础信息
     * TODO 本接口不涉及权限绑定
     *
     * @param executeData 待新增的用户基础信息数据
     * @return
     */
    @Access({AccessType.ADD})
    @Transactional(readOnly = false)
    public String addUserInfo(BaseData executeData) {
        //为了保证数据的强一致性，数据表ID将使用getTableIdByCode方法进行指向性获取
        return baseApi.insertData(getTableIdByCode("sys_user"), StringKit.parseSchemaExecuteData(executeData));
    }


    /**
     * 更新用户基础信息
     * TODO 本接口不涉及权限绑定
     *
     * @param executeData 待更新的用户基础信息数据
     * @return
     */
    @Access({AccessType.UPD})
    @Transactional(readOnly = false)
    public int updateUserInfo(BaseData executeData) {
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        int result = baseApi.updateData(getTableIdByCode("sys_user"), StringKit.parseSchemaExecuteData(executeData));
        if (result == 1) {
            //更新并重置当前用户数据缓存
            redisTool.delete("system:user:id:" + data.get("id"));
            selectUser(executeData);
        }
        return result;
    }


    /**
     * 用户黑名单（软删除）
     * TODO 将用户投入黑名单
     * TODO 通过本接口删除的数据可以找回及手动恢复
     *
     * @param executeData 待置入黑名单的用户
     *                    {id : xxxxxx}
     * @return
     */
    @Access({AccessType.DEL})
    @Transactional(readOnly = false)
    public int deleteSoftUser(BaseData executeData) {
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        //为了保证数据的强一致性，数据表ID将使用getTableIdByCode方法进行指向性获取
        int result = deleteDataSoft(enCapsulationData("sys_user", executeData));
        if (result == 1) {
            //更新并重置当前用户数据缓存
            redisTool.delete("system:user:id:" + data.get("id"));
            selectUser(executeData);
        }
        return result;
    }

    /**
     * 删除用户（暴力删除）
     * TODO 本接口只能删除在黑名单中的用户数据
     * TODO 通过本接口删除的数据无法进行找回及手动恢复
     *
     * @param executeData 待删除的用户
     *                    {id : xxxxxx}
     * @return
     */
    @Access({AccessType.DEL})
    @Transactional(readOnly = false)
    public int deleteUser(BaseData executeData) {
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        int result = deleteData(enCapsulationData("sys_user", executeData));
        if (result == 1) {
            //清除当前用户缓存
            redisTool.delete("system:user:id:" + data.get("id"));
        }
        return result;
    }


    /**
     * 在系统用户登陆时获取用户信息
     *
     * @param executeData 用户请求登录的对象
     *                    {
     *                    email :  用户的邮箱
     *                    ……
     *                    TODO : 示例为使用邮箱作为唯一账号进行登录，使用时请根据实际情况修改本方法即可
     *                    }
     * @return
     */
    @Access({AccessType.LOGIN, AccessType.SEL})
    public BaseData selectUserLogin(BaseData executeData) {
        //1.声明返回值
        BaseData user = null;
        //2.执行查询
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        List<BaseData> userList = baseApi.selectBase(new sqlEngine()
                .setSelect(" select * from sys_user where state = 0 and email = '" + StringEscapeUtils.escapeSql(data.get("email") + "") + "'"));
        if (userList.size() > 0) {
            //TODO 无论根据条件查询出了多少条数据，始终获取第1条
            user = userList.get(0);
        }
        //3.载入缓存
        if (user != null) {
            //将用户数存入Redis
            redisTool.set("system:user:id:" + user.get("id"), JSON.toJSONString(user), -1);
        }else{
            //空数据1分钟内不允许多次访问数据库进行查询
            redisTool.set("system:user:id:" + user.get("id"), "{}", 60000);
        }
        return user;
    }


    /**
     * 获取用户基础信息
     *
     * @param executeData 待获取的用户
     *                    {id : xxxxxx}
     * @return
     */
    @Access({AccessType.SEL})
    public BaseData selectUser(BaseData executeData) {
        //1.声明返回值
        BaseData user = null;
        //2.获取ID
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        //3.从Redis中尝试获取缓存数据
        String userData = redisTool.get("system:user:id:" + data.get("id"));
        if (userData.isEmpty() || userData == null) {
            //为了保证数据的强一致性，数据表ID将使用getTableIdByCode方法进行指向性获取
            user = selectByOne(enCapsulationData("sys_user", executeData));
            if (user != null) {
                //将用户数存入Redis
                redisTool.set("system:user:id:" + data.get("id"), JSON.toJSONString(user), -1);
            }else{
                //空数据1分钟内不允许多次访问数据库进行查询
                redisTool.set("system:user:id:" + data.get("id"), "{}", 60000);
            }
        } else {
            user = StringKit.parseBaseData(userData);
        }
        return user;
    }


}
