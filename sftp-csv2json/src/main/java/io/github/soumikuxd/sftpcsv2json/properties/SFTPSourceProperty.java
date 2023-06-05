package io.github.soumikuxd.sftpcsv2json.properties;

import com.sun.istack.NotNull;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ToString
@Generated
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.sftp.input")
public class SFTPSourceProperty {
    @NotNull
    private String host;
    @NotNull private String remoteDir;
    @NotNull private int port = 2222;
    @NotNull private String username;
    @NotNull private String password;
    @NotNull private String passiveMode = "false";
    @NotNull private String filenamePattern = "*.*";
    @NotNull private String delay="10";
    @NotNull private String recursive="true";
    @NotNull private String delete="false";
    @NotNull private String synchronous="true";
    @NotNull private String stepwise="false";
    @NotNull private String delimiter=";";
}