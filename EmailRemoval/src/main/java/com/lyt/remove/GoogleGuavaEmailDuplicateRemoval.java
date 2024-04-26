package com.lyt.remove;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * ClassName: GoogleGuavaEmailDuplicateRemoval
 * Package: com.lyt.filter
 * Description: 采用布隆过滤器去重
 * 主要步骤：
 * ① 引入布隆过滤器相关依赖（com.google.guava）
 * ② 创建布隆过滤器
 * ③ 对文件读取并过滤
 * @Author 彤彤
 * @Create 2024/4/25 17:21
 * @Version 1.0
 */
public class GoogleGuavaEmailDuplicateRemoval {
    private static final int EXPECTED_EMAILS = 100000000;  // 期望的E-Mail地址数量
    private static final double FALSE_POSITIVE_RATE = 0.01;  // 允许的错误判定率
    private static final String INPUT_FILE_PATH = "data/initial/emails.txt";  // 输入文件路径
//    private static final String OUTPUT_FILE_PATH = "google_guava_deduplicated_emails.txt";  // 输出文件路径
    private static final String OUTPUT_FILE_PATH = "data/removal/google_guava_deduplicated_emails.txt";  // 输出文件路径

    public static void main(String[] args) {
        BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), EXPECTED_EMAILS, FALSE_POSITIVE_RATE);

        try (BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE_PATH));
             FileWriter writer = new FileWriter(OUTPUT_FILE_PATH)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!bloomFilter.mightContain(line)) {
                    bloomFilter.put(line);
                    writer.write(line);
                    writer.write(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
