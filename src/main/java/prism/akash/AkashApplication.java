package prism.akash;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import prism.akash.container.BaseData;
import prism.akash.controller.BaseController;

import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
public class AkashApplication {

	@Autowired
	BaseController baseController;

	@Bean
	public void corsFilter() throws Exception{
//		for (int i = 0;i<100;i++){
		BaseData sel = new BaseData();
		sel.put("eid","1");
		sel.put("akname","2");
		sel.put("csize","3");
		sel.put("pn", "0");
		sel.put("ps", "10");

//
		String data = JSON.toJSONString(sel);
		System.out.println(baseController.selectBase("1",data));
		System.out.println(baseController.selectBase("2",data));
		System.out.println(baseController.selectBase("4",data));
		System.out.println(baseController.selectBase("3",data));

		BaseData ins = new BaseData();
		ins.put("name", "哈哈哈哈");
		System.out.println(baseController.insertBase("5",JSON.toJSONString(ins)));

		ins.put("copyvalue", "3");
		System.out.println(baseController.insertBase("6", JSON.toJSONString(ins)));

		List<BaseData> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			BaseData b = new BaseData();
			b.put("name", "数据批量测试" + i);
			list.add(b);
		}
		System.out.println(baseController.insertBase("7",JSON.toJSONString(list)));
//		System.out.println(baseController.selectBase("1",data));
//		System.out.println(baseController.selectBase("1",data));
//		}

	}

	public static void main(String[] args) {
		SpringApplication.run(AkashApplication.class, args);
	}

}
