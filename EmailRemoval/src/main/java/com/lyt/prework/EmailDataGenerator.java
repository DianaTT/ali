package com.lyt.prework;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
/**
 * ClassName: EmailDataGenerator
 * Package: com.lyt.prework
 * Description: 生成2GB的email文件
 * email生成定义限制因素：
 *  ① 只允许包含大小写字母与数字
 *  ② 格式满足 xxx@ali.com
 *  ③ ‘@’之前最大长度不超过64个字符（8个字节）
 * @Author 彤彤
 * @Create 2024/4/25 17:15
 * @Version 1.0
 */
public class EmailDataGenerator {
    private static final int MIN_USERNAME_LENGTH = 6;  // E-Mail地址的用户名部分最小长度
    private static final int MAX_USERNAME_LENGTH = 18;  // E-Mail地址的用户名部分最大长度
    private static final long FILE_SIZE = 2L * 1024L * 1024L * 1024L;  // 2GB文件大小
//    private static final long FILE_SIZE = 2L * 1024L * 1024L;  // 2MB文件大小
    private static final double DUPLICATE_PROBABILITY = 0.1;  // 生成邮箱的重复概率（10%的概率重复）


    public static void main(String[] args) {
        String outputFile = "data/initial/emails.txt";  // 输出文件路径

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            long bytesWritten = 0;

            while (bytesWritten < FILE_SIZE) {
                String email = generateRandomEmail();

                writer.write(email);
                writer.newLine();
                bytesWritten += email.getBytes().length + 1;  // +1 for newline character

                // 根据重复概率决定是否写入重复的地址
                if (Math.random() <= DUPLICATE_PROBABILITY) {
                    //重复写入
                    writer.write(email);
                    writer.newLine();
                    bytesWritten += email.getBytes().length + 1;  // +1 for newline character
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateRandomEmail() {
        String username = generateRandomUsername();
        String domain = generateRandomDomain();

        return username + "@" + domain;
    }

    private static String generateRandomUsername() {
        Random random = new Random();
        int length = random.nextInt(MAX_USERNAME_LENGTH - MIN_USERNAME_LENGTH + 1) + MIN_USERNAME_LENGTH;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char randomChar = (char) (random.nextInt(26) + 'a');  // Generate lowercase letters
            sb.append(randomChar);
        }

        return sb.toString();
    }

    private static String generateRandomDomain() {
        String[] domains = {"ali.com", "test.com", "domain.com", "sample.com"};
        Random random = new Random();
        int index = random.nextInt(domains.length);

        return domains[index];
    }
}
