package org.example.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Properties;

/**
 * 作者：Leo
 * 描述：永无bug
 */
@Configuration
public class KaptchaConfig {
    @Bean
    DefaultKaptcha producer() { //验证码的配置类
        Properties properties = new Properties();
        properties.put("kaptcha.border", "no");                       //边框
        properties.put("kaptcha.textproducer.font.color", "black");   //字体颜色
        properties.put("kaptcha.textproducer.char.space", "5");       //字体间隔
        properties.put("kaptcha.image.height", "50");                 //图片高度
        properties.put("kaptcha.image.width", "200");                 //图片宽度
        properties.put("kaptcha.textproducer.font.size", "40");       //字体大小
        properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑"); // 设置字体
        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
