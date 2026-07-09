package org.example.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 图片验证码配置。
 */
@Configuration
public class KaptchaConfig {
    /**
     * 配置验证码图片的大小、字体、字符间距等参数。
     */
    @Bean
    DefaultKaptcha producer() {
        Properties properties = new Properties();
        properties.put("kaptcha.border", "no");
        properties.put("kaptcha.textproducer.font.color", "black");
        properties.put("kaptcha.textproducer.char.space", "5");
        properties.put("kaptcha.image.height", "50");
        properties.put("kaptcha.image.width", "200");
        properties.put("kaptcha.textproducer.font.size", "40");
        properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");

        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
