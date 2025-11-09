package org.example.MybatisConfig;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;

import java.util.Collections;

/**
 * 作者：Leo
 * 描述：代码快速生成器
 */
public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/admin?allowPublicKeyRetrieval=true&useSSL=false&characterEncoding=utf-8",
                        "root",
                        "123456")
                .globalConfig(builder -> builder
                        .author("Leo")          // 作者
                        .outputDir(System.getProperty("user.dir") + "/src/main/java") // 输出路径
                        .disableOpenDir()             // 可选，不打开目录
                )
                .packageConfig(builder -> builder
                        .parent("org.example")
                        .moduleName("")
                        .pathInfo(Collections.singletonMap(OutputFile.xml,
                                System.getProperty("user.dir") + "/src/main/resources/mappers"))
                )
                .strategyConfig(builder -> builder
                        .addInclude("sys_user")           // 需要生成的表
                        .entityBuilder().enableLombok()
                        .controllerBuilder().enableRestStyle()
                )
                .execute();
    }
}
