package prism.akash.tools.analysis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import prism.akash.api.BaseApi;
import prism.akash.container.extend.BaseDataExtends;

/**
 * HTML5模板文件解析类
 */
@Component
public class HtmlTemplateAnalysis extends BaseDataExtends{

    //TODO : 获取系统默认的文件存储主路径
    @Value("${akashConfig.defaultFilePath}")
    public String defaultFilePath;

    @Autowired
    BaseApi baseApi;

}
