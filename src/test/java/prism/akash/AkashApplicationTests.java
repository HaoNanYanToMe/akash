package prism.akash;
import com.alibaba.fastjson.JSON;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import prism.akash.container.BaseData;
import prism.akash.controller.BaseController;
import prism.akash.controller.proxy.BaseProxy;
import prism.akash.tools.context.SpringContextUtil;
import prism.akash.tools.reids.RedisTool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 多并发线程测试
 * TODO  秒杀示例
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AkashApplicationTests {

    @Autowired
    RedisTool redisTool;

    @Autowired
    BaseController baseController;
//
    @Test
    public void testPrism(){
        //查询单条数据
        BaseData  bd = new BaseData();
        bd.put("id","57c4b85014b4447dbf21b0c6abcfe9f2");
        BaseData  execute = new BaseData();
        execute.put("id","80378c5a7b404fe59fb04943bbf9cd78");
        bd.put("executeData",JSON.toJSONString(execute));
        System.out.println(baseController.executeUnify("","selectByOne",JSON.toJSONString(bd)));
    }

    //更新菜单
    public String upd(){
        BaseData  bd = new BaseData();
        bd.put("id","57c4b85014b4447dbf21b0c6abcfe9f2");
        BaseData  execute = new BaseData();
        execute.put("name","测试22P");
        execute.put("code","ZZytest");
        execute.put("is_parent",1);
        execute.put("is_lock",0);
        execute.put("pid",-1);
        execute.put("note",-1);
        execute.put("order_number",0);
        execute.put("version",1);
        execute.put("id","80378c5a7b404fe59fb04943bbf9cd78");
        bd.put("executeData",JSON.toJSONString(execute));
        return  baseController.executeUnify("","updateData",JSON.toJSONString(bd));
    }

    //新增菜单
    public String add(){
        BaseData  bd = new BaseData();
        bd.put("id","57c4b85014b4447dbf21b0c6abcfe9f2");
        BaseData  execute = new BaseData();
        execute.put("name","测试");
        execute.put("code","test");
        execute.put("is_parent",1);
        execute.put("is_lock",0);
        execute.put("pid",-1);
        execute.put("order_number",0);
        bd.put("executeData",JSON.toJSONString(execute));
        return baseController.executeUnify("base","insertData", JSON.toJSONString(bd));
    }

    /**
     * TODO 本示例模拟演示了100个用户竞争10件商品的秒杀逻辑
     * @throws Throwable
     */
//    @Test
    public void testThreadJunit() throws Throwable {
        //设置基础库存数量
        redisTool.set("kucun","10",600000);

        //并发数量
        int runnerSize = 100;

        //Runner数组，相当于并发多少个。
        TestRunnable[] trs = new TestRunnable[runnerSize];

        for (int i = 0; i < runnerSize; i++) {
            trs[i] = new ThreadA();
        }

        // 用于执行多线程测试用例的Runner，将前面定义的单个Runner组成的数组传入
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);

        // 开发并发执行数组里定义的内容
        mttr.runTestRunnables();
    }

    private class ThreadA extends TestRunnable {
        @Override
        public void runTest() throws Throwable {
            String value =  redisTool.getOnLock("kucun");
            int other = Integer.parseInt(value) - 1;
            //库存抢占
            update("kucun",other);
        }
    }

    //Test:数据更新多线程测试
    //---->测试目标:无数据抢占情况出现
    public void update(String key,int other) throws Exception {
       if (other > -1){
           redisTool.set(key,other+"",600000);
           System.out.println("当前剩余库存："+select("kucun"));
       }else{
           System.out.println("抢占失败，库存不足");
       }
    }

    //Test:数据查询多线程测试
    //---->测试目标:可以准确返回指定查询数据
    public String select(String key) throws Exception {
        return redisTool.get(key);
    }
}
