package prism.akash.tools.analysis;

import gui.ava.html.image.generator.HtmlImageGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import prism.akash.container.BaseData;
import prism.akash.container.extend.BaseDataExtends;

import java.io.*;
import java.util.UUID;

/**
 * HTML5模板文件解析类
 */
@Component
public class HtmlTemplateAnalysis extends BaseDataExtends{

    //TODO : 获取系统默认的文件存储主路径
    @Value("${akashConfig.defaultFilePath}")
    public String defaultFilePath;


    public String generatePicture(String stayTempId){
        BaseData getUrl = new BaseData();
        getUrl.put("mid",stayTempId);



        HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
        String html = this.readHtml("D:/haibao/hb.html");
        imageGenerator.loadHtml(html);
        try {
            imageGenerator.getBufferedImage();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        UUID uid = UUID.randomUUID();
        imageGenerator.saveAsImage("d:/"+uid+".png");
        return "A";
    }

    public String readHtml(String htmlAddress) {
        String body = "";
        File file = new File(htmlAddress);

        if (file.exists()) {
            Reader reader;
            try (FileInputStream iStream = new FileInputStream(file)) {
                reader = new InputStreamReader(iStream);
                BufferedReader htmlReader = new BufferedReader(reader);
                String line;
                while ((line = htmlReader.readLine()) != null) {
                    if (line.contains("css/")) {
                        if (line.contains("link")) {
                            line = line.substring(line.indexOf("css/"), line.indexOf(".css\"")) + ".css";
                            StringBuffer css = new StringBuffer("<style>");
                            css.append(this.readHtml(defaultFilePath + line));
                            css.append("</style>");
                            body += css.toString() + "\n";
                        }
                    } else if (line.contains(".js")) {
                        //TODO : 忽略
                    } else {
                        body += line + "\n";
                    }
                }
                htmlReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return body;
        } else {
            return null;
        }
    }
}
