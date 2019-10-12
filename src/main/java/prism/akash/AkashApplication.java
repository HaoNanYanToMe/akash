package prism.akash;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import prism.akash.container.BaseData;
import prism.akash.controller.BaseController;


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
		String data = JSON.toJSONString(sel);
//		System.out.println(baseController.selectBase("1",data));
		System.out.println(baseController.selectBase("1",data));
		System.out.println(baseController.selectBase("2",data));
//		System.out.println(baseController.selectBase("1",data));
//		System.out.println(baseController.selectBase("1",data));
//		}

	}

	public static void main(String[] args) {
		SpringApplication.run(AkashApplication.class, args);
	}

}
