package prism.akash.controller.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import prism.akash.tools.file.FileHandler;
import prism.akash.tools.file.FileUpload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文件管理相关接口
 *       TODO : 系统·文件管理 （上传 / 下载 / 处理）
 * @author HaoNan Yan
 */
@RestController
public class FileController implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    FileHandler fileHandler;

    @Autowired
    FileUpload fileUpload;

    /**
     * 文件上传
     *
     * @param request
     * @param file
     * @return
     * @throws Exception
     */
    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping(value = "/upFile",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String upLoad(
            HttpServletRequest request,
            @RequestParam(value = "file", required = false) MultipartFile[] file
    ) {
        String files = "";
        //文件处理
        String ffile = new SimpleDateFormat("yyyyMMdd").format(new Date());
        if (null != file && file.length != 0) {
            try {
                files = fileHandler.uploadFiledList(file, request, "" + ffile);
            } catch (Exception e) {
                logger.error("FileController:upLoad:Exception -> " + e.getMessage() + " / " + e.getCause().getMessage());
            }
        }
        return files;
    }


    /**
     * 获取图片及文件流
     *
     * @param response
     * @param fileName
     * @throws IOException
     */
    @CrossOrigin(origins = "*", maxAge = 3600)
    @RequestMapping(value = "/getFile",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public void getFile(
            HttpServletResponse response,
            @RequestParam(value = "fileName", required = false) String fileName
    ) {
        try {
            fileHandler.getFile(response, fileName);
        } catch (IOException e) {
            logger.error("FileController:getFile:IOException -> " + e.getMessage() + " / " + e.getCause().getMessage());
        }
    }
}
