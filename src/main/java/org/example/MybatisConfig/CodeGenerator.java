package org.example.MybatisConfig;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;

import java.util.Collections;

/**
 * MyBatis-Plus 代码生成器。
 * <p>
 * 只在本地手动执行 main 方法时使用，不参与项目运行。
 */
public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/admin?allowPublicKeyRetrieval=true&useSSL=false&characterEncoding=utf-8",
                        "root",
                        "123456")
                .globalConfig(builder -> builder
                        .author("Leo")
                        .outputDir(System.getProperty("user.dir") + "/src/main/java")
                        .disableOpenDir()
                )
                .packageConfig(builder -> builder
                        .parent("org.example")
                        .moduleName("")
                        .pathInfo(Collections.singletonMap(OutputFile.xml,
                                System.getProperty("user.dir") + "/src/main/resources/mappers"))
                )
                .strategyConfig(builder -> builder
                        .addInclude("sys_user")
                        .entityBuilder().enableLombok()
                        .controllerBuilder().enableRestStyle()
                )
                .execute();
    }
}
