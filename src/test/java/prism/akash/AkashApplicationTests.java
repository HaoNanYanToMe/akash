package prism.akash;

import java.util.Random;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class AkashApplicationTests {

    public static void main(String[] args) {
        Random random = new Random();

        for (int i =0 ; i < 6 ; i++) {
            //产生随机数
            int number = random.nextInt(36 - 1 + 1) + 1;

            //打印随机数
            System.out.println(number);
        }

        int number = random.nextInt(16 - 1 + 1) + 1;

        //打印随机数
        System.out.println(number);
    }
}
