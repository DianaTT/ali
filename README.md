# ali
阿里云——email大文件小内存去重

### 题目描述
  有一个文件，文件内容是E-Mail地址，已知文件大小约2GB，每行一个E-Mail地址， 但有部分E-Mail地址是重复的，现在有一台VM，VM内存为1GB，业务需要向这些 E-Mail发送邮件，但不能重复发，所以需要写一个程序对E-Mail地址进行去重。
### 挑战点
  大文件处理，小VM内存
### 解题方法
  #### 方法一：采用布隆过滤器进行email去重
  #### 方法二：采用分批处理加外部排序进行email去重
### 具体过程
1. 生成email大文件
     - 自定义email生成定义限制因素
     - 生成email大文件（约2GB）
2. 对email大文件进行去重操作
   1. 采用布隆过滤器进行email去重（google的guava）
        1. 引入布隆过滤器相关依赖（com.google.guava）
        2. 创建布隆过滤器
        3. 对文件读取并过滤
   2. 采用分批处理加外部排序进行email去重
      1. 分批处理获得临时分批文件
      2. 对临时分批文件去重
      3. 将临时分批文件外部排序归并整理
3. 设置VM内存为1GB
      "-Xms1g -Xmx1g"
4. 生成去重后的email文件
### 目录结构
* EmailRemoval
  * data    (数据)
    * initial    (初始化生成的email大文件)
      * emails.txt    (初始化的email大文件)
    * removal    (去重后的email大文件)
      * google_guava_deduplicated_emails.txt    (方法一去重后的文件)
      * large_file_duplicate_removal_emails.txt    (方法二去重后的文件)
    * test    (采用2MB数据VM内存1MB小数量级实验相关数据)
      * small_emails.txt    (初始化的email小文件)
      * small_google_emails.txt    (方法一去重后的小文件)
      * test_small_emails.txt    (方法二去重后的小文件)
  * src    (代码)
    * main
      * java
        * com
          * lyt
            * prework    (前置工作)
              * EmailDataGenerator.java    (生成email大文件)
            * remove    (去重操作)
              * GoogleGuavaEmailDuplicateRemoval.java    (方法一去重：布隆过滤器)
              * LargeFileDuplicateRemoval.java    (方法二去重：分批处理加外部排序)
### 额外信息
由于生成的文件过大，没有上传，可直接运行验证。
