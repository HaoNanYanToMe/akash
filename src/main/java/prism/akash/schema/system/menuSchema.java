package prism.akash.schema.system;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.schema.BaseSchema;
import prism.akash.tools.StringKit;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统菜单相关逻辑类
 *       TODO : 系统·核心逻辑 （独立）
 * @author HaoNan Yan
 */
@Service
public class menuSchema extends BaseSchema {

    /**
     * 获取系统默认菜单树
     *
     * @param executeData
     *              {
     *                  systemName 系统名称
     *              }
     * @return
     */
    public List<BaseData> getRootMenuTree(BaseData executeData) {
        List<BaseData> mTree = redisTool.getList("system:menu:root:tree", null, null);
        if (mTree.isEmpty() || mTree == null) {
            BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
            List<BaseData> list = new ArrayList<>();

            BaseData tree = new BaseData();
            tree.put("id", 0);

            tree.put("title", data.get("systemName"));
            tree.put("expand", true);
            tree.put("is_lock", false);
            tree.put("version", 0);
            tree.put("children", this.getMenuNode("-1"));

            list.add(tree);

            mTree = list;
            redisTool.set("system:menu:root:tree", mTree, -1);
        }
        return mTree;
    }


    /**
     * TODO 递归
     * 根据指定的节点获取菜单信息
     *
     * @param pid
     * @return
     */
    private List<BaseData> getMenuNode(String pid) {
        //查询并获取当前递归节点数据信息
        //TODO 仅查询当前为正常的数据节点
        String selectPid = "select id,name,code,state,order_number,is_parent,version from sys_menu where pid = '" + pid + "' and state = 0 order by order_number asc";
        List<BaseData> menuList = baseApi.selectBase(new sqlEngine().setSelect(selectPid));
        List<BaseData> list = new ArrayList<>();
        for (BaseData menu : menuList) {
            //menuId不为空时，当前节点有效
            if (menu.get("id") != null) {
                BaseData mTree = new BaseData();
                mTree.put("id", menu.get("id"));
                mTree.put("title", menu.get("name"));
                //默认节点不展开
                mTree.put("expand", false);
                mTree.put("version", menu.get("version"));
                mTree.put("is_lock", menu.get("state").equals("0") ? false : true);
                //判断当前节点是否为父节点
                //TODO  0-否 / 1-是
                if (menu.get("is_parent").equals("1"))
                    mTree.put("children", getMenuNode(menu.getString("id")));

                list.add(mTree);
            }
        }
        return list;
    }

    /**
     * 根据ID获取指定菜单节点的信息
     *
     * @param executeData 待获取的数据节点ID
     *                    {
     *                    id: 数据节点id
     *                    }
     * @return
     */
    public BaseData getMenuNodeData(BaseData executeData) {
        BaseData result = selectByOne(enCapsulationData("sys_menu", executeData));
        //如果获取值为空,则锁定当前数据1分钟,1分钟内禁止对数据库进行访问
        if (result == null) {
            redisTool.set("system:menu:id:" + result, new BaseData(), 60000);
        } else {
            redisTool.set("system:menu:id:" + result, executeData, -1);
        }
        return result;
    }

    /**
     * 内部方法：对单数据节点的增删改缓存操作进行优化提出
     * @param id    数据节点id
     * @return
     */
    private BaseData redisCache(String id) {
        BaseData select = new BaseData();
        select.put("id", id);
        return getMenuNodeData(pottingData("sys_menu", select));
    }


    /**
     * 新增菜单节点
     *
     * @param executeData Menu节点的数据对象
     * @return
     */
    @Transactional(readOnly = false)
    public String addMenuNode(BaseData executeData) {
        //为了保证数据的强一致性，数据表ID将使用getTableIdByCode方法进行指向性获取
        String result = baseApi.insertData(getTableIdByCode("sys_menu"), StringKit.parseSchemaExecuteData(executeData));
        if (!result.equals("")) {
            if (!result.equals("-1") && !result.equals("-2")) {
                redisCache(result);
                //TODO 新增成功时,重置redis缓存
                redisTool.delete("system:menu:root:tree");
            }
        }
        return result;
    }

    /**
     * 更新菜单节点
     *
     * @param executeData Menu节点的待更新数据对象
     * @return
     */
    @Transactional(readOnly = false)
    public int updateMenuNode(BaseData executeData) {
        //为了保证数据的强一致性，数据表ID将使用getTableIdByCode方法进行指向性获取
        int result = baseApi.updateData(getTableIdByCode("sys_menu"), StringKit.parseSchemaExecuteData(executeData));
        if (result == 1) {
            BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
            redisCache(data.get("id") + "");
            //TODO 更新成功,重置redis缓存
            redisTool.delete("system:menu:root:tree");
        }
        return result;
    }

    /**
     * 删除菜单节点
     *
     * @param executeData Menu节点的待删除的对象
     *                    {id : xxxxxx}
     * @return
     */
    @Transactional(readOnly = false)
    public int deleteMenuNode(BaseData executeData) {
        int result = 0;
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        //查询当前节点下是否拥有子节点
        int size = baseApi.selectBase(new sqlEngine().setSelect(" select id from  sys_menu where pid = '" + data.get("id") + "'")).size();
        if (size == 0) {
            //TODO 使用软删除对数据进行更新操作
            //为了保证数据的强一致性，数据表ID将使用getTableIdByCode方法进行指向性获取
            result = deleteDataSoft(enCapsulationData("sys_menu", executeData));
            if (result == 1) {
                redisTool.delete("system:menu:id:" + data.get("id"));
                //TODO 删除成功,重置redis缓存
                redisTool.delete("system:menu:root:tree");
            }
        }
        return result;
    }
}
