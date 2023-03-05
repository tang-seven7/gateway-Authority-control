package com.seven.springcloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "auth.ignore")
public class WhiteListProperties {
    private List<String> whites;
}
