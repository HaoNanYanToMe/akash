package prism.akash.schema.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.schema.BaseSchema;
import prism.akash.tools.StringKit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户权限相关类
 * TODO : 系统·核心逻辑 （独立）
 *
 * @author HaoNan Yan
 */
@Service("userRoleSchema")
@Transactional(readOnly = true)
public class userRoleSchema extends BaseSchema {

    @Autowired
    userSchema userSchema;

    @Autowired
    roleSchema roleSchema;

    /**
     * 用户授权操作
     * TODO 通过权限树进行点选
     *
     * @param executeData 授权对象
     *                    {
     *                    *uid: 用户id    TODO 必填字段
     *                    *rid: 权限id    TODO 必填字段
     *                    }
     * @return
     */
    @Transactional(readOnly = false)
    public String bindUserRole(BaseData executeData) {
        String result = "-5";
        //解析executeData
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        //TODO 数据强校验
        //进行授权时,uid及rid不能为null
        if (data.get("uid") != null && data.get("rid") != null) {
            String uid = data.getString("uid");
            String rid = data.getString("rid");
            //0.判断uid及rid是否存在
            data.put("id", data.get("uid"));
            BaseData user = userSchema.selectUser(pottingData("", data));
            data.put("id", data.get("rid"));
            BaseData role = roleSchema.getRoleNodeData(pottingData("", data));
            if (user == null) {
                result = "UR1";
            } else if (role == null) {
                result = "UR2";
            } else {
                //1.先获取已有权限，没有则新增，有则删除
                List<BaseData> userData = inspectRole(uid, rid);
                if (userData.size() == 0) {
                    //获取最新的数据下标
                    List<BaseData> allRole = redisTool.getList("system:user:role:id:" + uid, null, null);
                    int newOrder = 0;
                    if (allRole.size() > 0) {
                        newOrder = allRole.get(allRole.size() - 1).getInter("order_number") + 1;
                    }
                    //执行新增
                    result = addUserRole(uid, rid, newOrder);
                } else {
                    //执行删除
                    result = deleteUserRole(userData.get(0).getString("id"), uid) + "";
                }
            }
        }
        return result;
    }

    /**
     * 获取当前用户拥有的权限信息
     *
     * @param executeData {
     *                    userId: 用户id
     *                    }
     * @return
     */
    public List<BaseData> getCurrentRole(BaseData executeData) {
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        String userId = data.getString("uid");
        //从redis里获取当前用户的权限缓存
        List<BaseData> userData = redisTool.getList("system:user:role:id:" + userId, null, null);
        if (userData.size() == 0) {
            //未获取到缓存数据，请求数据
            String roles = "select ur.id,ur.uid,ur.rid,ur.state,ur.order_number,r.name,r.note,r.is_supervisor " +
                    "from sys_userrole ur left join sys_role r on r.id = ur.rid " +
                    "where r.state = 0 and  ur.uid = '" + userId + "' and ur.state = 0 order by ur.order_number asc";
            userData = baseApi.selectBase(new sqlEngine().setSelect(roles));
            if (userData.size() > 0) {
                redisTool.set("system:user:role:id:" + userId, userData, -1);
            }
        }
        return userData == null ? new ArrayList<>() : userData;
    }

    /**
     * 内部方法 : 权限检测
     *
     * @param userId 用户编号
     * @param roleId 指定的权限编号
     * @return
     */
    private List<BaseData> inspectRole(String userId, String roleId) {
        BaseData user = new BaseData();
        user.put("uid", userId);
        //从redis里获取当前用户的权限缓存
        List<BaseData> userData = getCurrentRole(pottingData("", user));
        //TODO 检测当前权限是否已存在
        return userData.stream().filter(ur -> (ur.get("rid") + "").equals(roleId)).collect(Collectors.toList());
    }

    /**
     * 内部方法：新增用户授权
     *
     * @param userId       用户编号
     * @param roleId       指定的权限编号
     * @param order_number 当前授权序列号
     * @return
     */
    @Transactional(readOnly = false)
    protected String addUserRole(String userId, String roleId, int order_number) {
        BaseData executeData = new BaseData();
        executeData.put("uid", userId);
        executeData.put("rid", roleId);
        executeData.put("order_number", order_number);

        return insertData(pottingData("sys_userrole", executeData));
    }

    /**
     * 内部方法：移除用户授权
     *
     * @param ur_id  授权信息id
     * @param userId 用户id
     * @return
     */
    @Transactional(readOnly = false)
    protected int deleteUserRole(String ur_id, String userId) {
        String delete = "delete from sys_userrole where id = '" + ur_id + "'";
        int result = baseApi.execute(new sqlEngine().setExecute(delete));
        if (result > 0) {
            //删除成功后,将redis缓存重置
            redisTool.delete("system:user:role:id:" + userId);
        }
        return result;
    }
}
