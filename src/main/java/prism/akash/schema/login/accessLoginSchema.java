package prism.akash.schema.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prism.akash.container.BaseData;
import prism.akash.schema.BaseSchema;
import prism.akash.schema.system.menuDataSchema;
import prism.akash.schema.system.roleMenuSchema;
import prism.akash.schema.system.userRoleSchema;
import prism.akash.schema.system.userSchema;
import prism.akash.tools.StringKit;
import prism.akash.tools.annocation.Access;
import prism.akash.tools.annocation.checked.AccessType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 系统用户
 * ※主要用于系统用户登陆鉴权及统一登录态管理
 * TODO : 方法仅提供给LoginController使用
 *
 * TODO : 系统·基础核心逻辑 （独立）
 *
 * @author HaoNan Yan
 */
@Service("accessLoginSchema")
@Transactional(readOnly = true)
public class accessLoginSchema extends BaseSchema {

    @Autowired
    userSchema userSchema;

    @Autowired
    userRoleSchema userRoleSchema;

    @Autowired
    roleMenuSchema roleMenuSchema;

    @Autowired
    menuDataSchema menuDataSchema;


    /**
     * 用户鉴权登陆
     *
     * @param executeData 用户登陆时设置的信息（通过统一登录鉴权后的数据）
     *                    {
     *                    id   :   用户在系统sys_user表中的id
     *                    rid  :   用户指定登陆时使用的权限
     *                    }
     * @return {
     * isLogin     : 登陆状态，true/false
     * loginTips   : 登陆状态提示
     * roleList    : 可使用权限列表  TODO isLogin为false时存在
     * role        : 当前使用的权限
     * menu        : 当前权限可访问的菜单
     * }
     */
    @Access({AccessType.SEL, AccessType.LOGIN})
    public Map<String, Object> accessLogin(BaseData executeData) {
        Map<String, Object> result = new ConcurrentHashMap<>();
        //0.解析proxy的executeData的数据
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        //1.获取用户信息
        BaseData user = userSchema.selectUser(executeData);
        //2.获取当前用户可用权限
        BaseData getRole = new BaseData();
        getRole.put("uid", user.get("id"));
        executeData.put("executeData", getRole);
        List<BaseData> roleList = userRoleSchema.getCurrentRole(pottingData("", getRole));
        if (roleList.size() > 1) {
            //如果当前用户拥有多个权限
            //TODO 并且未指定需要使用的权限时
            if (data.get("rid") == null || data.get("rid").equals("")) {
                result.put("roleList", roleList);
                result.put("isLogin", false);
                result.put("loginTips", "抱歉，请先选择或指定您需要使用的系统权限");
            } else {
                result = accessRoleOne(data.getString("rid"), roleList);
            }
        } else {
            if (roleList.size() == 1) {
                //当权限只有一个时，系统将默认自动使用该权限
                result = accessRoleOne(roleList.get(0).getString("rid"), roleList);
            } else {
                result.put("isLogin", false);
                result.put("loginTips", "抱歉，您的账号当前暂未获得系统访问权限");
            }
        }
        return result;
    }

    /**
     * 内部方法提取：指定（单一）权限授权登陆
     *
     * @param rid      指定的权限
     * @param roleList 当前用户可使用的权限
     * @return
     */
    private Map<String, Object> accessRoleOne(String rid, List<BaseData> roleList) {
        Map<String, Object> result = new ConcurrentHashMap<>();
        Boolean isLogin = false;
        //判断当前权限是否真实有效
        List<BaseData> roleExist = roleList.stream().filter(r -> r.getString("rid").equals(rid)).collect(Collectors.toList());
        //TODO rid为空也视为未指定权限
        if (rid.isEmpty() || roleExist.size() == 0) {
            result.put("loginTips", "抱歉，您的账号当前暂未获得系统访问权限");
        } else {
            isLogin = true;
            result.put("role", roleExist.get(0));
            //获取可访问的菜单数据
            BaseData menuRole = new BaseData();
            menuRole.put("rid", rid);
            //获取菜单树
            List<BaseData> menuList = roleMenuSchema.getCurrentMenu(pottingData("", menuRole));
            result.put("menu", menuTree(menuList, "-1"));
            result.put("loginTips", "授权访问成功");
            //将当前用户权限可访问的数据集合载入缓存
            getLoginAccess(menuList, rid);
        }
        result.put("isLogin", isLogin);
        return result;
    }

    /**
     * 根据菜单树循环获取可访问数据（表 / Schema原生逻辑对象）集合
     *
     * @param menuList     当前权限可使用菜单列表
     * @param rid          当前使用的权限id
     * @return
     */
    public List<BaseData> getLoginAccess(List<BaseData> menuList, String rid) {
        List<BaseData> roleData = redisTool.getList("login:role_data:id:" + rid, null, null);
        if (roleData.size() == 0) {
            for (BaseData menu : menuList) {
                List<BaseData> menuData = menuDataSchema.getCurrentAccessData(pottingData("", menu));
                if (menuData.size() > 0) {
                    for (BaseData m : menuData) {
                        m.put("page_role", menu.get("page_role"));
                        m.put("page_normal_role", menu.get("page_normal_role"));
                        roleData.add(m);
                    }
                }
            }
            //TODO 为方便后期同权限用户使用,首次加载将同步至缓存
            //判断roleData是否有数据，如果没有，则置空，且60s内无法再次获取
            boolean dataExist = roleData.size() == 0;
            redisTool.set("login:role_data:id:" + rid, dataExist ? new ArrayList<>() : roleData, dataExist ? -1 : 60000);
        }
        return roleData;
    }


    /**
     * 获取可供前台使用的菜单树
     * TODO 将getCurrentMenu获取的缓存数据进行二次处理
     *
     * @param menuList
     * @param pid      父节点id
     * @return
     */
    private List<BaseData> menuTree(List<BaseData> menuList, String pid) {
        List<BaseData> list = new ArrayList<>();
        //通过lambda对数据进行分析处理
        List<BaseData> filterData = menuList.stream().filter(t -> t.get("pid").equals(pid)).collect(Collectors.toList());
        for (BaseData menu : filterData) {
            BaseData mTree = new BaseData();
            mTree.put("id", menu.get("mid"));
            mTree.put("icon", menu.get("icon"));
            mTree.put("path", menu.get("path"));
            mTree.put("name", menu.get("code"));
            mTree.put("title", menu.get("name"));
            //判断当前节点是否为父节点
            //TODO  0-否 / 1-是
            if (menu.getInter("is_parent") == 1){
                mTree.put("children", menuTree(menuList,menu.getString("mid")));
            }

            list.add(mTree);
        }
        return list;
    }
}
