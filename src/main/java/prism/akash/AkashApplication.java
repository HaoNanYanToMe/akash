package prism.akash;

import com.alibaba.fastjson.JSON;
import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.controller.BaseController;
import prism.akash.tools.analysis.HtmlTemplateAnalysis;
import prism.akash.tools.asyncInit.AsyncInitData;
import prism.akash.tools.file.FileUpload;

import java.io.FileInputStream;


@SpringBootApplication
public class AkashApplication {

	@Autowired
	BaseApi baseApi;

	@Autowired
	HtmlTemplateAnalysis htmlTemplateAnalysis;

	@Autowired
	AsyncInitData asyncInitData;

	@Autowired
	FileUpload fileUpload;

	@Autowired
	BaseController baseController;

	@Bean
	public void corsFilter() throws Exception{
		BaseData sel = new BaseData();
		sel.put("eid","1");
		sel.put("akname","%2%");
		sel.put("csize","3");
		sel.put("pn", "0");
		sel.put("ps", "10");
//
		String data = JSON.toJSONString(sel);
		System.out.println(baseController.select("1",data));
		System.out.println(baseController.select("2",data));
		System.out.println(baseController.select("4",data));
		System.out.println(baseController.select("3",data));
//		List<BaseData> asy = asyncInitData.asyncInitDataBase();
//		for (BaseData t:asy) {
//			if (t.get("code").equals("sourceqdata")){
//				System.out.println(JSON.toJSONString(t));
//				System.out.println("新增"+t.get("code")+":" + (baseController.initData(t.get("code")+"#"+t.get("name")+" ", JSON.toJSONString(t.get("colimns"))) == "1" ?"成功":"失败"));
//
//			}
//		}

//		InputStream inputStream = new FileInputStream("D:/2.xlsx");
//		ByteArrayOutputStream output = new ByteArrayOutputStream();
//		byte[] buffer = new byte[1024 * 4];
//		int n = 0;
//		while (-1 != (n = inputStream.read(buffer))) {
//			output.write(buffer, 0, n);
//		}
//		BaseData bd = new BaseData();
//		bd.put("execute","sourceqdata");
//		fileUpload.importExcel("D:/2.xlsx",bd,true,10000);
	}

	public static void main(String[] args) {
		SpringApplication.run(AkashApplication.class, args);
	}

}
