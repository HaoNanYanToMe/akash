package prism.akash;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import prism.akash.container.BaseData;
import prism.akash.tools.file.FileUpload;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AkashApplicationTests {

    @Autowired
    FileUpload fileUpload;

    @Test
    public void test() throws Exception{
        BaseData bd = new BaseData();
        bd.put("execute","bs_dataq");
        fileUpload.importExcel("E:\\weFiles\\WeChat Files\\tianliangleccns\\FileStorage\\File\\2020-06\\data.xlsx",bd,true,500);
    }
}
