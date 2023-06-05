package io.github.soumikuxd.sftpcopy.properties;

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
@ConfigurationProperties(prefix = "app.sftp.output")
public class SFTPTargetProperty {
    @NotNull private String host;
    @NotNull private String remoteDir;
    @NotNull int port = 2222;
    @NotNull private String username;
    @NotNull private String password;
}
