package prism.akash.schema.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.schema.BaseSchema;
import prism.akash.tools.StringKit;
import prism.akash.tools.annocation.Access;
import prism.akash.tools.annocation.Schema;
import prism.akash.tools.annocation.checked.AccessType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统权限菜单相关类
 * TODO : 系统·核心逻辑 （独立）
 *
 * @author HaoNan Yan
 */
@Service
@Transactional(readOnly = true)
@Schema(code = "roleMenu", name = "系统基础逻辑管理")
public class roleMenuSchema extends BaseSchema {

    @Autowired
    menuSchema menuSchema;

    @Autowired
    roleSchema roleSchema;

    @Autowired
    reloadMenuDataSchema reloadMenuDataSchema;

    /**
     * 菜单授权操作
     * TODO 通过菜单树进行点选
     *
     * @param executeData 授权对象
     *                    {
     *                    *mid: 菜单id    TODO 必填字段
     *                    *rid: 权限id    TODO 必填字段
     *                    }
     * @return
     */
    @Access({AccessType.ADD, AccessType.DEL})
    @Transactional(readOnly = false)
    public String bindRoleMenu(BaseData executeData) {
        String result = "-5";
        //解析executeData
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        //TODO 数据强校验
        //进行授权时,uid及rid不能为null
        if (data.get("mid") != null && data.get("rid") != null) {
            String mid = data.getString("mid");
            String rid = data.getString("rid");
            //0.判断uid及rid是否存在
            data.put("id", mid);
            BaseData menu = menuSchema.getMenuNodeData(pottingData("", data));
            data.put("id", rid);
            BaseData role = roleSchema.getRoleNodeData(pottingData("", data));
            if (menu == null) {
                result = "UR3";
            } else if (role == null) {
                result = "UR2";
            } else {
                //1.先获取已绑定的菜单，没有则新增，有则删除
                List<BaseData> menuData = inspectMenu(mid, rid);
                if (menuData.size() == 0) {
                    //获取最新的数据下标
                    List<BaseData> allRole = redisTool.getList("system:role_menu:id:" + mid, null, null);
                    int newOrder = 0;
                    if (allRole.size() > 0) {
                        newOrder = allRole.get(allRole.size() - 1).getInter("order_number") + 1;
                    }
                    //执行新增
                    result = addRoleMenu(mid, rid, newOrder);
                } else {
                    //执行删除
                    result = deleteUserRole(menuData.get(0).getString("id"), rid) + "";
                }
                //4.将指定权限缓存重置
                reloadMenuDataSchema.reloadLoginData(rid);
            }
        }
        return result;
    }


    /**
     * 获取当前权限可访问的菜单列表
     *
     * @param executeData {
     *                    rid: 权限id
     *                    }
     * @return
     */
    @Access({AccessType.SEL})
    public List<BaseData> getCurrentMenu(BaseData executeData) {
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        String roleId = data.getString("rid");
        //从redis里获取当前权限的菜单缓存数据
        List<BaseData> menuData = redisTool.getList("system:role_menu:id:" + roleId, null, null);
        if (menuData.size() == 0) {
            //未获取到缓存数据，请求数据
            String menus = " select rm.*,m.name,m.note,m.pid,m.code,m.icon,m.path,m.component,m.is_parent," +
                    " m.version,m.state" +
                    " from sys_rolemenu rm left join sys_menu m on m.id = rm.mid " +
                    " where m.state = 0 and rm.state = 0 " +
                    " and rm.rid =  '" + roleId + "' order by rm.order_number asc ";
            menuData = baseApi.selectBase(new sqlEngine().setSelect(menus));
            if (menuData.size() > 0) {
                redisTool.set("system:role_menu:id:" + roleId, menuData, -1);
            }
        }
        return menuData == null ? new ArrayList<>() : menuData;
    }


    /**
     * 内部方法 : 菜单权限检测
     *
     * @param menuId 菜单编号
     * @param roleId 指定的权限编号
     * @return
     */
    private List<BaseData> inspectMenu(String menuId, String roleId) {
        BaseData menu = new BaseData();
        menu.put("rid", roleId);
        //从redis里获取当前用户的权限缓存
        List<BaseData> menuData = getCurrentMenu(pottingData("", menu));
        //TODO 检测当前权限是否已存在
        return menuData.stream().filter(rm -> (rm.get("mid") + "").equals(menuId)).collect(Collectors.toList());
    }

    /**
     * 内部方法：新增菜单授权
     *
     * @param menuId       菜单编号
     * @param roleId       指定的权限编号
     * @param order_number 当前授权序列号
     * @return
     */
    @Transactional(readOnly = false)
    protected String addRoleMenu(String menuId, String roleId, int order_number) {
        BaseData executeData = new BaseData();
        executeData.put("mid", menuId);
        executeData.put("rid", roleId);
        executeData.put("order_number", order_number);
        String result = insertData(pottingData("sys_rolemenu", executeData));
        if (result.length() == 32) {
            //新增成功后,将redis缓存重置
            redisTool.delete("system:role_menu:id:" + roleId);
        }
        return result;
    }


    /**
     * 内部方法：移除菜单授权
     *
     * @param um_id  授权信息id
     * @param roleId 权限id
     * @return
     */
    @Transactional(readOnly = false)
    protected int deleteUserRole(String um_id, String roleId) {
        String delete = "delete from sys_rolemenu where id = '" + um_id + "'";
        int result = baseApi.execute(new sqlEngine().setExecute(delete));
        if (result > 0) {
            //删除成功后,将redis缓存重置
            redisTool.delete("system:role_menu:id:" + roleId);
        }
        return result;
    }

}
