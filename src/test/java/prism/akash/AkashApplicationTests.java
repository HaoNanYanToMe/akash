package prism.akash;


import org.springframework.beans.factory.annotation.Autowired;
import prism.akash.tools.file.FileUpload;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class AkashApplicationTests {

//	@Test
//	public void contextLoads() {
//	}
    @Autowired
    FileUpload fileUpload;

    public static void main(String[] args) throws IOException, ParseException {
        System.out.println( new SimpleDateFormat("yyyyMMdd").parse("20180101"));
    }
}
