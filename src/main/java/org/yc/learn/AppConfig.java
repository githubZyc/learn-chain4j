package org.yc.learn;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 统一的应用配置入口，所有 main 方法都从这里取值。
 *
 * 取值优先级：环境变量 > classpath 下的 config.properties
 *
 * @author zhengyanchuang
 */
public final class AppConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPS = load();

    private AppConfig() {
    }

    public static QwenChatModel qwenChatModel() {
        return QwenChatModel.builder().apiKey(apiKey()).modelName(modelName())
                .isMultimodalModel(true).build();
    }

    public static QwenStreamingChatModel qwenStreamingChatModel() {
        return QwenStreamingChatModel.builder().apiKey(apiKey()).modelName(modelName())
                .isMultimodalModel(true).build();
    }

    public static String apiKey() {
        // 兼容旧的小写环境变量名
        return get("api_key", "DASHSCOPE_API_KEY", "api_key");
    }

    public static String modelName() {
        return get("model_name", "MODEL_NAME", "model_name");
    }

    public static boolean isMultimodal() {
        String value = firstNonBlank(System.getenv("IS_MULTIMODAL"), System.getenv("is_multimodal"));
        if (value == null) {
            value = PROPS.getProperty("is_multimodal", "false");
        }
        return Boolean.parseBoolean(value);
    }

    private static String get(String key, String... envNames) {
        String[] envValues = new String[envNames.length];
        for (int i = 0; i < envNames.length; i++) {
            envValues[i] = System.getenv(envNames[i]);
        }
        String value = firstNonBlank(envValues);
        if (value == null) {
            value = PROPS.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "缺少配置项 " + key + "：请设置环境变量 " + String.join(" / ", envNames)
                            + "，或复制 config.properties.example 为 config.properties 并填写");
        }
        return value.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取 " + CONFIG_FILE + " 失败", e);
        }
        return props;
    }
}
