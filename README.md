# code-generator

一些自用的代码生成小工具，仅提供参考思路，可能需要根据实际情况进行修改。

## Generate MyBatis Code.groovy

用于 MyBatis 代码生成，生成数据库表对应的 Entity、Mapper、Service、ServiceImpl 类。

使用方法：

放置在 IDEA 的 extensions/com.intellij.database/schema 目录下，然后在 database 面板里右键表名依次选择 Scripted Extensions、Generate MyBatis Code.groovy，在弹出在对应框中选择想要放置代码的目录，点击 OK 即可。

详细说明参考 <https://mazhuang.org/2024/09/24/custom-idea-mybatis-code-generator/>

## JUnit4 Test Class.velocity

用于在 IDEA 中生成 JUnit4 测试类时，自动注入 logger 和被测 Service。

使用方法：

在 IDEA 的 Preferences | Editor | File and Code Templates 的 Code 选项卡中，找到 JUnit4 Test Class，将 JUnit4 Test Class.velocity 文件内容复制到对应的文本框中，点击 OK 即可。

详细说明参考 <https://mazhuang.org/2024/09/25/custom-junit4-test-class/>
