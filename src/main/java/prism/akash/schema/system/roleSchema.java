package prism.akash.schema.system;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.schema.BaseSchema;
import prism.akash.tools.StringKit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 系统权限相关逻辑类
 *       TODO : 系统·核心逻辑 （独立）
 * @author HaoNan Yan
 */
@Service
public class roleSchema extends BaseSchema {

    /**
     * ①获取系统默认权限树
     *
     * @param systemName 系统名称
     * @return
     */
    public String getRootRoleTree(String systemName) {
        String mTree = redisTool.get("system:role:root:tree");
        if (mTree.isEmpty() || mTree == null) {
            List<BaseData> list = new ArrayList<>();

            BaseData tree = new BaseData();
            tree.put("id", 0);

            tree.put("title", systemName);
            tree.put("expand", true);
            tree.put("is_lock", false);
            tree.put("version", 0);
            tree.put("children", this.getRoleNode("-1"));

            list.add(tree);

            mTree = JSON.toJSONString(list);
            redisTool.set("system:role:root:tree", mTree, -1);
        }
        return mTree;
    }


    /**
     * TODO 递归
     * 根据指定的节点获取权限信息
     *
     * @param pid
     * @return
     */
    private List<BaseData> getRoleNode(String pid) {
        //查询并获取当前递归节点数据信息
        String selectPid = "select id,name,code,state,order_number,is_parent,version from sys_role where pid = '" + pid + "' order by order_number asc";
        List<BaseData> roleList = baseApi.selectBase(new sqlEngine().setSelect(selectPid));
        List<BaseData> list = new ArrayList<>();
        for (BaseData role : roleList) {
            //roleId不为空时，当前节点有效
            if (role.get("id") != null) {
                BaseData rTree = new BaseData();
                rTree.put("id", role.get("id"));
                rTree.put("title", role.get("name"));
                //默认节点不展开
                rTree.put("expand", false);
                rTree.put("version", role.get("version"));
                rTree.put("is_lock", role.get("state").equals("0") ? false : true);
                //判断当前节点是否为父节点
                //TODO  0-否 / 1-是
                if (role.get("is_parent").equals("1"))
                    rTree.put("children", getRoleNode(role.getString("id")));

                list.add(rTree);
            }
        }
        return list;
    }

    /**
     * 新增权限节点
     *
     * @param executeData 权限节点的数据对象
     * @return
     */
    @Transactional(readOnly = false)
    public String addRoleNode(String executeData) {
        //为了保证数据的强一致性，数据表ID将使用getTableIdByCode方法进行指向性获取
        String result = baseApi.insertData(getTableIdByCode("sys_role"), executeData);
        if (!result.equals("")) {
            if (!result.equals("-1") && !result.equals("-2")) {
                //TODO 新增成功时,重置redis缓存
                redisTool.delete("system:role:root:tree");
            }
        }
        return result;
    }

    /**
     * 更新权限节点
     *
     * @param executeData 权限节点的待更新数据对象
     * @return
     */
    @Transactional(readOnly = false)
    public int updateRoleNode(String executeData) {
        //为了保证数据的强一致性，数据表ID将使用getTableIdByCode方法进行指向性获取
        int result = baseApi.updateData(getTableIdByCode("sys_role"), executeData);
        if (result == 1) {
            //TODO 更新成功,重置redis缓存
            redisTool.delete("system:role:root:tree");
        }
        return result;
    }

    /**
     * 删除权限节点
     *
     * @param executeData 权限节点的待删除的对象
     *                    {id : xxxxxx}
     * @return
     */
    @Transactional(readOnly = false)
    public int deleteRoleNode(String executeData) {
        int result = 0;
        //查询当前节点下是否拥有子节点
        LinkedHashMap<String, Object> params = StringKit.parseLinkedMap(executeData);
        int size = baseApi.selectBase(new sqlEngine().setSelect(" select id from  sys_role where pid = '" + params.get("id") + "'")).size();
        if (size == 0) {
            //为了保证数据的强一致性，数据表ID将使用getTableIdByCode方法进行指向性获取
            result = baseApi.deleteData(getTableIdByCode("sys_role"), executeData);
            if (result == 1) {
                //TODO 删除成功,重置redis缓存
                redisTool.delete("system:role:root:tree");
            }
        }
        return result;
    }

}
