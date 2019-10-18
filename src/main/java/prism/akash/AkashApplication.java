package prism.akash;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import prism.akash.container.BaseData;
import prism.akash.controller.BaseController;
import prism.akash.tools.analysis.HtmlTemplateAnalysis;
import prism.akash.tools.asyncInit.AsyncInitData;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;


@SpringBootApplication
public class AkashApplication {

	@Autowired
	BaseController baseController;

	@Autowired
	HtmlTemplateAnalysis htmlTemplateAnalysis;

	@Autowired
	AsyncInitData asyncInitData;

	@Bean
	public void corsFilter() throws Exception{

		List<BaseData> asy = asyncInitData.asyncInitDataBase();

		for (BaseData t:asy) {
			System.out.println("新增"+t.get("code")+":" + (baseController.initData(t.get("code")+"#"+t.get("name")+" ", JSON.toJSONString(t.get("colimns"))) == "1" ?"成功":"失败"));
		}


//		for (int i = 0;i<100;i++){
		BaseData sel = new BaseData();
//		sel.put("eid","1");
		sel.put("akname","%2%");
		sel.put("csize","3");
		sel.put("pn", "0");
		sel.put("ps", "10");
//
		String data = JSON.toJSONString(sel);
		//		System.out.println(baseController.select("1",data));
//		System.out.println(baseController.selectBase("2",data));
//		System.out.println(baseController.selectBase("4",data));
//		System.out.println(baseController.selectBase("3",data));
		BaseData ins = new BaseData();
		ins.put("name", "测试测试");
		ins.put("name2", "测试测试2");


		BaseData init = new BaseData();
		init.put("id", "ID");
		init.put("name", "名称");
//		init.put("type","引擎类型");
//
//		System.out.println(baseController.initData("testAkash#测试数据表", JSON.toJSONString(init)));

//		System.out.println(baseController.insertBase("1",JSON.toJSONString(ins)));
//		BaseData upd = new BaseData();
//		System.out.println(baseController.insertBase("8",JSON.toJSONString(upd)));
//		BaseData ins = new BaseData();
//		ins.put("name", "哈哈哈哈");
//		System.out.println(baseController.insertBase("5",JSON.toJSONString(ins)));
//
//		ins.put("copyvalue", "3");
//		System.out.println(baseController.insertBase("6", JSON.toJSONString(ins)));
//
//		List<BaseData> list = new ArrayList<>();
//		for (int i = 0; i < 10; i++) {
//			BaseData b = new BaseData();
//			b.put("name", "数据批量测试" + i);
//			list.add(b);
//		}
//		System.out.println(baseController.insertBase("7",JSON.toJSONString(list)));
//		System.out.println(baseController.selectBase("1",data));
//		System.out.println(baseController.selectBase("1",data));
//		}

	}

	public static void main(String[] args) {
		SpringApplication.run(AkashApplication.class, args);
	}

}
