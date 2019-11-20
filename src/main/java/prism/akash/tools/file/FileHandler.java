package prism.akash.tools.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Component
public class FileHandler {

    //TODO : 获取系统默认的文件存储主路径
    @Value("${akashConfig.defaultFilePath}")
    public String defaultFilePath;

    /**
     * 根据文件名称获取指定的文件流信息
     * @param response
     * @param fileName   文件名称
     * @throws IOException
     */
    public void getFile(HttpServletResponse response, String fileName) throws IOException {
        ServletOutputStream out = null;
        FileInputStream ips = null;
        try {
            //获取图片存放路径
            String imgPath = defaultFilePath + File.separator +  fileName;
            ips = new FileInputStream(new File(imgPath));
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            response.setContentType("multipart/form-data");
            out = response.getOutputStream();
            //读取文件流
            int len = 0;
            byte[] buffer = new byte[1024 * 10];
            while ((len = ips.read(buffer)) != -1){
                out.write(buffer,0,len);
            }
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            out.close();
            ips.close();
        }
    }
}
