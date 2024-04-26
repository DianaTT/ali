package com.lyt.remove;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * ClassName: LargeFileDuplicateRemoval
 * Package: com.lyt.filter
 * Description: 采用大文件分批处理和外部排序进行去重
 * 主要步骤：
 * ① 分批处理获得临时分批文件
 * ② 对临时分批文件去重
 * ③ 将临时分批文件外部排序归并整理
 * @Author 彤彤
 * @Create 2024/4/25 19:45
 * @Version 1.0
 */


public class LargeFileDuplicateRemoval {
    private static final int BATCH_SIZE = 30000000; // 每个批次处理的行数
//    private static final int BATCH_SIZE = 50000; // 每个批次处理的行数（小文件处理）
    private static final String INPUT_FILE_PATH = "data/initial/emails.txt"; // 输入文件路径
    private static final String TEMP_FILE_PREFIX = "temp_batch_"; // 临时文件的前缀

    private static final String OUTPUT_FILE_PATH = "data/removal/large_file_duplicate_removal_emails.txt";  // 输出文件路径

    public static void main(String[] args) {
        try {
            // 创建临时文件夹
            Path tempDirectory = Files.createTempDirectory("temp");
            System.out.println("临时文件夹路径：" + tempDirectory.toString());

            // 分批处理数据
            splitAndProcessData(tempDirectory);

            // 合并结果
            mergeResults(tempDirectory);

            // 删除临时文件夹
            Files.walk(tempDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分批处理获得临时分批文件
     * @param tempDirectory
     * @throws IOException
     */
    private static void splitAndProcessData(Path tempDirectory) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE_PATH))) {
            String line = reader.readLine();
            int batchNumber = 1;

            while (line != null) {
                // 获取当前批次的临时文件路径
                Path tempFilePath = tempDirectory.resolve(TEMP_FILE_PREFIX + batchNumber + ".txt");

                // 写入当前批次的数据到临时文件
                try (BufferedWriter writer = Files.newBufferedWriter(tempFilePath)) {
                    for (int i = 0; i < BATCH_SIZE; i++) {
                        if (line != null) {
                            writer.write(line);
                            writer.newLine();
                        } else {
                            break; // 当 line 为 null 时，跳出循环
                        }

                        line = reader.readLine();
                    }
                }

                // 对当前批次的数据进行去重
                deduplicateBatch(tempFilePath);

                batchNumber++;
            }
        }
    }

    /**
     * 对临时分批文件去重
     * @param batchFilePath
     * @throws IOException
     */
    private static void deduplicateBatch(Path batchFilePath) throws IOException {
        Set<String> uniqueEmails = new HashSet<>();
        //读取分批文件
        try (BufferedReader reader = new BufferedReader(new FileReader(batchFilePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                uniqueEmails.add(line);
            }
        }

        // 将去重后的结果写入临时文件
        try (BufferedWriter writer = Files.newBufferedWriter(batchFilePath)) {
            for (String email : uniqueEmails) {
                writer.write(email);
                writer.newLine();
            }
        }
    }

    /**
     * 将临时分批文件外部排序归并整理
     * @param tempDirectory
     * @throws IOException
     */
    private static void mergeResults(Path tempDirectory) throws IOException {
        List<Path> tempFiles = new ArrayList<>();

        // 获取临时文件列表
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDirectory, TEMP_FILE_PREFIX + "*.txt")) {
            for (Path path : stream) {
                tempFiles.add(path);
            }
        }

        // 对临时文件进行外部排序
        Collections.sort(tempFiles);

        // 合并结果到最终输出文件
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(OUTPUT_FILE_PATH))) {
            for (Path tempFile : tempFiles) {
                try (BufferedReader reader = new BufferedReader(new FileReader(tempFile.toFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
        }
    }
}