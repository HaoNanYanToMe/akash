package prism.akash;

import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.engineEnum.conditionType;
import prism.akash.container.sqlEngine.engineEnum.queryType;
import prism.akash.container.sqlEngine.sqlEngine;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class AkashApplicationTests {

//	@Test
//	public void contextLoads() {
//	}

    public static void main(String[] args) {
        sqlEngine engine = new sqlEngine();

//        String s = engine.execute(new sqlEngine().execute("test","ttt").appointColumn("test","p1,p2,p3").selectFin(),"testChild").appointColumn("testChild","p1#看看吧").selectFin().parseSql().getString("select");
//        System.out.println(s);
    }
}
