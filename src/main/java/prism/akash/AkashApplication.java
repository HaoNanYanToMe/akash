package prism.akash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.controller.BaseController;
import prism.akash.tools.analysis.HtmlTemplateAnalysis;
import prism.akash.tools.asyncInit.AsyncInitData;
import prism.akash.tools.file.FileUpload;

import java.util.List;


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

	}

	public static void main(String[] args) {
		SpringApplication.run(AkashApplication.class, args);
	}

}
