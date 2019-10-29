package prism.akash.tools.analysis;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import gui.ava.html.image.generator.HtmlImageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.extend.BaseDataExtends;
import prism.akash.container.sqlEngine.engineEnum.*;
import prism.akash.container.sqlEngine.sqlEngine;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

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

    /**
     * 获取生成图片
     * @param stayTempId    使用的模板ID
     * @param indexSort     使用的主模板编号（考虑单模板多样式的情况）
     * @param keyData       关键词替换json
     * @return
     */
    public String generatePicture(String stayTempId,String indexSort,String keyData){
        BaseData getUrl = new BaseData();
        getUrl.put("mid",stayTempId);

        sqlEngine sel = new sqlEngine();
        sel.execute("modelfolder","m")
                .joinBuild("modelfile","f", joinType.L)
                .joinColunm("m","id","mid")
                .joinFin()
                .appointColumn("m", groupType.DEF,"id,name,code")
                .appointColumn("f",groupType.DEF,"url,indexSort")
                .queryBuild(queryType.and,"f","@mid", conditionType.EQ,groupType.DEF,stayTempId)
                .queryBuild(queryType.and,"f","@indexSort", conditionType.EQ,groupType.DEF,indexSort)
                .dataSort("f","indexSort", sortType.ASC).selectFin("");
        List<BaseData> index = baseApi.selectBase(sel);

        if (index.size() > 0){
            HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
            //TODO:  获取真实文件地址路径
            String url = defaultFilePath + File.separator + index.get(0).get("code");
            String html = this.readHtml(url + File.separator + index.get(0).get("url"),url);


            //TODO:  置换参数
            LinkedHashMap<String, Object> params = JSONObject.parseObject(keyData, new TypeReference<LinkedHashMap<String, Object>>() {
            });

            for (String key : params.keySet()) {
                html = html.replaceAll("@" + key, params.get(key) + "");
            }

            imageGenerator.loadHtml(html);
            try {
                imageGenerator.getBufferedImage();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            UUID uid = UUID.randomUUID();
            imageGenerator.saveAsImage(defaultFilePath + File.separator + uid + ".png");
            return uid.toString();
        }else{
            return "";
        }
    }

    private String readHtml(String htmlAddress,String url) {
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
                            css.append(this.readHtml(url + File.separator + line,url));
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
            } catch (NullPointerException x){
                x.printStackTrace();
            }
            return body;
        } else {
            return null;
        }
    }
}
