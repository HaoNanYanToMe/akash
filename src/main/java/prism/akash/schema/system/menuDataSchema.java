package prism.akash.schema.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.schema.BaseSchema;
import prism.akash.tools.StringKit;
import prism.akash.tools.annocation.Access;
import prism.akash.tools.annocation.checked.AccessType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统菜单可用数据（逻辑）相关类
 * -- 本类方法主要提供应用于access访问权限认证
 * TODO : 系统·核心逻辑 （独立）
 *
 * @author HaoNan Yan
 */
@Service("menuDataSchema")
@Transactional(readOnly = true)
public class menuDataSchema extends BaseSchema {

    @Autowired
    menuSchema menuSchema;

    @Autowired
    reloadMenuDataSchema reloadMenuDataSchema;

    /**
     * 授权菜单可使用（访问）的数据表 / 逻辑类
     * TODO 使用穿梭框进行授权操作，一对多
     *
     * @param executeData 授权信息对象
     *                    {
     *                    *mid :   菜单id                                         TODO 必填字段
     *                    *data:   数据表 / 逻辑id , 支持多个，多个之间用,隔开即可    TODO 必填字段
     *                    }
     * @return
     */
    @Access({AccessType.ADD, AccessType.DEL})
    @Transactional(readOnly = false)
    public String bindMenuData(BaseData executeData) {
        String result = "UR4";
        //解析executeData
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        //TODO 数据强校验
        //进行授权时,mid及data不能为null
        if (data.get("mid") != null && data.get("data") != null) {
            String mid = data.getString("mid");
            String dataList = data.getString("data");
            data.put("id", data.get("mid"));
            BaseData menu = menuSchema.getMenuNodeData(pottingData("", data));
            if (menu == null) {
                result = "UR3";
            } else {
                //1.新增前需要先将此前的绑定信息全部移除
                removeBind(mid);
                //从redis获取核心数据表的集合对象
                List<BaseData> tableList = getTableList();
                //2.解析dataList数据，循环执行新增
                int i = 0;
                for (String d : dataList.split(",")) {
                    if (!d.isEmpty() && d != null) {
                        String re = addMenuData(mid, d, i, tableList);
                        //当新增返回值为32位时即为新增成功
                        if (re.length() == 32) {
                            i++;
                            result = "1";
                        }
                    }
                }
                //3.执行完成，将数据载入缓存
                getCurrentAccessData(executeData);
                //4.将指定权限缓存重置
                reloadMenuDataSchema.reloadLoginData(executeData);
            }
        }
        return result;
    }

    /**
     * 获取当前菜单已授权可访问的数据列表
     *
     * @param executeData {
     *                    *mid: 菜单id
     *                    }
     * @return
     */
    @Access({AccessType.SEL})
    public List<BaseData> getCurrentAccessData(BaseData executeData) {
        BaseData data = StringKit.parseBaseData(executeData.getString("executeData"));
        String menuId = data.getString("mid");
        //从redis里获取当前菜单的数据缓存
        List<BaseData> menuData = redisTool.getList("system:menu_data:id:" + menuId, null, null);
        if (menuData.size() == 0) {
            //未获取到缓存数据，请求数据
            String menu = " select md.*,t.name,t.code from sys_menudata md left join cr_tables t on t.id = md.tid " +
                    "where md.state = 0 and md.mid = '" + menuId + "' and t.state = 0" +
                    " order by md.order_number asc ";
            menuData = baseApi.selectBase(new sqlEngine().setSelect(menu));
            if (menuData.size() > 0) {
                redisTool.set("system:menu_data:id:" + menuId, menuData, -1);
            }
        }
        return menuData == null ? new ArrayList<>() : menuData;
    }


    /**
     * 内部方法：新增菜单与数据表间的绑定关系
     *
     * @param menuId       菜单id
     * @param dataId       数据表id
     * @param order_number 序列号
     * @param tableList    已有数据表集合对象
     * @return
     */
    @Transactional(readOnly = false)
    protected String addMenuData(String menuId, String dataId, int order_number, List<BaseData> tableList) {
        String result = "";
        if (tableList.size() > 0) {
            //TODO 校验数据表对象是否存在
            int exist = tableList.stream().filter(t -> t.get("id").equals(dataId)).collect(Collectors.toList()).size();
            if (exist > 0) {
                BaseData executeData = new BaseData();
                executeData.put("mid", menuId);
                executeData.put("tid", dataId);
                executeData.put("order_number", order_number);

                result = insertData(pottingData("sys_menudata", executeData));
            }
        }
        return result;
    }

    /**
     * 内部方法：移除数据表绑定信息
     *
     * @param menuId
     * @return
     */
    @Transactional(readOnly = false)
    protected int removeBind(String menuId) {
        String delete = "delete from sys_menudata where mid = '" + menuId + "'";
        int result = baseApi.execute(new sqlEngine().setExecute(delete));
        if (result > 0) {
            //删除成功后,将redis缓存重置
            redisTool.delete("system:menu_data:id:" + menuId);
        }
        return result;
    }
}
