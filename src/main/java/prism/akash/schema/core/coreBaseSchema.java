package prism.akash.schema.core;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.schema.BaseSchema;
import prism.akash.tools.StringKit;
import prism.akash.tools.annocation.Access;
import prism.akash.tools.annocation.checked.AccessType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 核心基础数据管理（逻辑）类
 * ※主要用于数据表及数据字段的新增、编辑及删除
 *
 * TODO : 系统·基础核心逻辑 （独立）
 *
 * @author HaoNan Yan
 */
@Service("coreBaseSchema")
@Transactional(readOnly = true)
public class coreBaseSchema extends BaseSchema {


    //TODO cr_table相关

    /**
     * 新增数据表或逻辑类
     *
     * @param executeData 数据表或逻辑类数据对象
     *                    {
     *                    *name :   名称
     *                    *code :   编码
     *                    *note :   注释
     *                    *type :   0-数据表 1-逻辑类
     *                    }
     * @return
     */
    @Access({AccessType.ADD})
    @Transactional(readOnly = false)
    public String addTable(BaseData executeData) {
        String result = "";
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        //为了保证数据的强一致性，数据表ID将使用getTableIdByCode方法进行指向性获取
        if (data.get("type") != null) {
            //确认当前表code是否存在
            //code作为数据表唯一键不允许重复
            if (!checkTableExist(data.get("code") + "")){
                result = baseApi.insertData(getTableIdByCode("cr_tables"), StringKit.parseSchemaExecuteData(executeData));
                if (!result.equals("")) {
                    if (!result.equals("-1") && !result.equals("-2")) {
                        //新增成功时对数据库进行操作创建新表，仅type为0时使用
                        if (data.getInter("type") == 0){

                        }
                        //TODO 新增成功时,重置redis缓存
                        redisTool.delete("core:table:list");
                    }
                }
            }
        }
        return result;
    }


    /**
     * 更新数据表或逻辑类
     *
     * @param executeData 数据表或逻辑类数据对象
     *                    {
     *                    *name :   名称
     *                    *code :   编码
     *                    *note :   注释
     *                    }
     * @return
     */
    @Access({AccessType.UPD})
    @Transactional(readOnly = false)
    public int updateTable(BaseData executeData) {
        //为了保证数据的强一致性，数据表ID将使用getTableIdByCode方法进行指向性获取
        int result = baseApi.updateData(getTableIdByCode("cr_tables"), StringKit.parseSchemaExecuteData(executeData));
        if (result == 1) {
            //TODO 更新成功,重置redis缓存
            redisTool.delete("core:table:list");
        }
        return result;
    }


    /**
     * 删除数据表或逻辑类
     * TODO 因为涉及到核心信息，所以仅开放软删除
     *
     * @param executeData 数据表或逻辑类对象id
     *                    {id : xxxxxx }
     * @return
     */
    @Access({AccessType.DEL})
    @Transactional(readOnly = false)
    public int deleteTable(BaseData executeData) {
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        int result = deleteDataSoft(enCapsulationData("cr_tables", executeData));
        if (result == 1) {
            //TODO 删除成功,重置redis缓存
            redisTool.delete("core:table:list");
            removeRelationMenuData(data.get("id") + "");
        }
        return result;
    }

    /**
     * 内部方法：判断当前code是否存在
     *
     * @param code
     * @return
     */
    private boolean checkTableExist(String code) {
        List<BaseData> tables = getTableList();
        return tables.stream().filter(t -> t.getString("code").equals(code)).collect(Collectors.toList()).size() > 0;
    }


    /**
     * 解除与菜单绑定的关联数据
     *
     * @param tableId 数据表 / 逻辑类id
     * @return 解绑成功的数量
     */
    private int removeRelationMenuData(String tableId) {
        int unbind = 0;
        //获取当前tableId绑定的菜单列表
        String menu = " select md.mid from sys_menudata md " +
                " where md.state = 0 and md.tid = '" + tableId + "' and md.type = 0" +
                " order by md.order_number asc ";
        List<BaseData> menuData = baseApi.selectBase(new sqlEngine().setSelect(menu));
        if (menuData.size() > 0) {
            //删除与当前表关联的全部数据
            String menuDataUnBind = " delete from sys_menudata where tid = '" + tableId + "'";
            unbind = baseApi.execute(new sqlEngine().setExecute(menuDataUnBind));
            if (unbind > 0) {
                for (BaseData m : menuData) {
                    //TODO 循环移除与当前表或逻辑有关的[菜单数据]缓存
                    redisTool.delete("system:menu_data:id:" + m.get("mid"));
                }
            }
        }
        return unbind;
    }
}
