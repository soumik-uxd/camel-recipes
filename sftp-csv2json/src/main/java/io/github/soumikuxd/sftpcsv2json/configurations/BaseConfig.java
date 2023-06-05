package io.github.soumikuxd.sftpcsv2json.configurations;

import com.sun.istack.NotNull;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app.base")
@Getter
public class BaseConfig {
    @NotNull
    private int initialDelaySeconds = 10;
    @NotNull
    private String appHeader = "sftp-copy";
}
