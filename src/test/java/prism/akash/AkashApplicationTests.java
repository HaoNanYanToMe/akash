package prism.akash;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import prism.akash.tools.reids.RedisTool;

/**
 * 多并发线程测试
 * TODO  秒杀示例
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AkashApplicationTests {

    @Autowired
    RedisTool redisTool;

    /**
     * TODO 本示例模拟演示了100个用户竞争10件商品的秒杀逻辑
     * @throws Throwable
     */
    @Test
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
