package com.lyt.remove;

import java.io.*;
import java.nio.file.*;
import java.util.*;
/**
 * ClassName: ExternalSortDeduplication
 * Package: com.lyt.remove
 * Description: 采用大文件划分内部排序去重和外部多路归并进行去重
 * 主要步骤：
 * ① 大文件划分为小文件
 * ② 对于每个小文件，内部排序去重
 * ③ 外部采用优先队列和当前行映射表实现多路归并去重
 * @Author 彤彤
 * @Create 2024/4/26 22:21
 * @Version 1.0
 */

public class ExternalSortDeduplication {
//    测试2MB小文件，VM内存1MB时是否可靠
//    private static final long MAX_MEMORY_SIZE = 1L * 1024L * 1024L; // 最大内存大小为 1MB
//    private static final String INPUT_FILE_PATH = "data/test/small_emails.txt"; // 输入文件路径
//    private static final String OUTPUT_FILE_PATH = "data/test/t.txt"; // 输出文件路径

    private static final long MAX_MEMORY_SIZE = 100L * 1024L * 1024L; // 最大内存大小为 100MB
    private static final String INPUT_FILE_PATH = "data/initial/emails.txt"; // 输入文件路径
    private static final String OUTPUT_FILE_PATH = "data/removal/external_sort_deduplicated_emails.txt"; // 输出文件路径
    private static final String TEMP_FILE_PREFIX = "temp_batch_"; // 临时文件前缀

    public static void main(String[] args) {
        try {
            //创建临时文件路径
            Path tempDirectory = Files.createTempDirectory("external_sort");
            //将大文件划分为小文件并排序去重
            splitAndSortData(tempDirectory);
            //将小文件归并
            mergeResults(tempDirectory);
            //删除临时文件
            deleteTempDirectory(tempDirectory);
            System.out.println("大文件Email去重成功完成！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将大文件划分为小文件并排序去重
     * @param tempDirectory
     * @throws IOException
     */
    private static void splitAndSortData(Path tempDirectory) throws IOException {
        //读取输入文件内容，分批处理
        try (BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE_PATH))) {
            List<String> batchLines = new ArrayList<>(); // 一批次的数据
            String line; // 读取行数据
            long currentSize = 0; //当前容量
            // 若当前行数据不为空，或者，读取容量不超过设置的最大内存 MAX_MEMORY_SIZE 限制，则继续读取
            // 否则，分批排序去重存储临时小文件
            while ((line = reader.readLine()) != null) {
                batchLines.add(line);
                currentSize += line.getBytes().length + System.lineSeparator().getBytes().length;

                if (currentSize >= MAX_MEMORY_SIZE) {
                    sortAndSaveBatch(tempDirectory, batchLines);
                    batchLines.clear();
                    currentSize = 0;
                }
            }

            if (!batchLines.isEmpty()) {
                sortAndSaveBatch(tempDirectory, batchLines);
            }
        }
    }

    /**
     * 小文件排序去重，结果写入临时文件
     * @param tempDirectory
     * @param batchLines
     * @throws IOException
     */
    private static void sortAndSaveBatch(Path tempDirectory, List<String> batchLines) throws IOException {
        Collections.sort(batchLines); // 对小文件进行排序
        //对小文件进行去重操作
        Set<String> uniqueEmails = new HashSet<>();
        Path batchFilePath = Files.createTempFile(tempDirectory, TEMP_FILE_PREFIX, ".txt");
        //读取小文件
        for(String batchLine:batchLines){
            uniqueEmails.add(batchLine);
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
     * 多路归并小文件结果
     * @param tempDirectory
     * @throws IOException
     */
    private static void mergeResults(Path tempDirectory) throws IOException {
        // 记录临时文件路径
        List<Path> tempFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDirectory, TEMP_FILE_PREFIX + "*.txt")) {
            for (Path path : stream) {
                tempFiles.add(path);
            }
        }

        // 优先队列用于按照读取器的内容自动排序
        PriorityQueue<BufferedReader> queue = new PriorityQueue<>(Comparator.comparing(BufferedReader::toString));
        // 当前行映射表，用于记录每个读取器与之对应的当前行数据
        Map<BufferedReader, String> currentLines = new HashMap<>();
        String preLine = ""; // 记录前一行数据，用于去重比较
        // 读取临时文件并初始化优先队列和当前行映射表
        for (Path tempFile : tempFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(tempFile.toFile()));
            queue.offer(reader);
            currentLines.put(reader, reader.readLine());
        }
        // 多路归并去重写入输出文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE_PATH))) {
            while (!queue.isEmpty()) {
                // 获取当前最小行读取器以及最小行数据
                BufferedReader minReader = queue.poll();
                String minLine = currentLines.get(minReader);
                // 若当前最小行数据和上一行不同，则写入
                if (minLine!=null && !minLine.equals(preLine)) {
                    writer.write(minLine);
                    writer.newLine();
                    preLine = minLine;
                }
                // 获取下一行数据，若不为空则加入优先队列和当前行映射表
                // 否则，关闭读取器，移除当前行映射表
                String nextLine = minReader.readLine();
                if (nextLine != null) {
                    currentLines.put(minReader, nextLine);
                    queue.offer(minReader);
                } else {
                    minReader.close();
                    currentLines.remove(minReader);
                }
            }
        }
        // 最后，将临时文件删除
        for (Path tempFile : tempFiles) {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * 删除临时文件
     * @param tempDirectory
     * @throws IOException
     */
    private static void deleteTempDirectory(Path tempDirectory) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDirectory)) {
            for (Path path : stream) {
                Files.deleteIfExists(path);
            }
        }
        Files.deleteIfExists(tempDirectory);
    }
}