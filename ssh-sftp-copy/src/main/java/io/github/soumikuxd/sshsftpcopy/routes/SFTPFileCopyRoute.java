package io.github.soumikuxd.sshsftpcopy.routes;

import io.github.soumikuxd.sshsftpcopy.configurations.BaseConfig;
import io.github.soumikuxd.sshsftpcopy.properties.SFTPSourceProperty;
import io.github.soumikuxd.sshsftpcopy.properties.SFTPTargetProperty;
import org.apache.camel.builder.RouteBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.security.auth.login.CredentialNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

@Component
public class SFTPFileCopyRoute extends RouteBuilder {
    @Autowired
    private SFTPSourceProperty sftpSourceProperty;

    @Autowired
    private SFTPTargetProperty sftpTargetProperty;

    @Autowired
    private BaseConfig baseConfig;

    @Override
    public void configure() throws Exception {
        // Determine the src credential to be used
        final Map.Entry<String, String> srcCredential = getCredential(this.sftpSourceProperty.getPassword(),
                                                                    this.sftpSourceProperty.getSshKey(),
                                                                    this.sftpSourceProperty.getSshKeyPath())
                                                        .entrySet().iterator().next();
        final String srcCredentialKey = srcCredential.getKey();
        final String srcCredentialValue = srcCredential.getValue();

        // Determine the target credential to be used
        final Map.Entry<String, String> targetCredential = getCredential(this.sftpTargetProperty.getPassword(),
                                                                        this.sftpTargetProperty.getSshKey(),
                                                                        this.sftpTargetProperty.getSshKeyPath())
                                                            .entrySet().iterator().next();;
        final String targetCredentialKey = targetCredential.getKey();
        final String targetCredentialValue = targetCredential.getValue();

        // Bind credentials to context
        getCamelContext().getRegistry().bind("srcCredential", srcCredentialValue);
        getCamelContext().getRegistry().bind("targetCredential", targetCredentialValue);

        URI fromSFTPURL = new URIBuilder()
                .setScheme("sftp")
                .setHost(this.sftpSourceProperty.getHost())
                .setPort(this.sftpSourceProperty.getPort())
                .setPath(this.sftpSourceProperty.getRemoteDir())
                .addParameter("noop", "true")
                .addParameter("username", this.sftpSourceProperty.getUsername())
                .addParameter(srcCredentialKey,"#srcCredential") // Add credential
                .addParameter("passiveMode", this.sftpSourceProperty.getPassiveMode())
                .addParameter("antInclude", this.sftpSourceProperty.getFilenamePattern())
                .addParameter("initialDelay", this.baseConfig.getInitialDelaySeconds() + "s")
                .addParameter("delay", this.sftpSourceProperty.getDelay())
                .addParameter("recursive", this.sftpSourceProperty.getRecursive())
                .addParameter("delete", this.sftpSourceProperty.getDelete())
                .addParameter("synchronous", this.sftpSourceProperty.getSynchronous())
                .addParameter("stepwise", this.sftpSourceProperty.getStepwise())
                .addParameter("idempotent", "true")
                .addParameter("idempotentKey", "${headers:CamelFileAbsolutePath}")
                .build();

        log.info("Download: " + fromSFTPURL.toString());

        URI toSFTPURL = new URIBuilder()
                .setScheme("sftp")
                .setHost(this.sftpTargetProperty.getHost())
                .setPort(this.sftpTargetProperty.getPort())
                .setPath(this.sftpTargetProperty.getRemoteDir())
                .addParameter("username", this.sftpTargetProperty.getUsername())
                .addParameter(targetCredentialKey,"#targetCredential") // Add credential
                .build();

        log.info("Upload: " + toSFTPURL.toString());

        from(fromSFTPURL.toString())
                .setHeader("appId").simple(this.baseConfig.getAppHeader())
                .setHeader("fileName").simple("${headers:CamelFileName}")
                .log("Started ${headers.CamelFileName}")
                .setHeader("fileName").simple("${headers.CamelFileName}")
                .setHeader("CamelFileName").simple("${headers.fileName}")
                .to(toSFTPURL.toString())
                .end()
                .log("Processing of ${headers.CamelFileName} complete.")
        ;

    }

    private Map<String, String> getCredential(String password, String sshKey, String sshKeyPath) throws IOException, CredentialNotFoundException {
        if (sshKey != null && !sshKey.isEmpty()) {
            log.info("Using private key as credential");
            return Collections.singletonMap("privateKey", sshKey);
        } else if (sshKeyPath != null && !sshKeyPath.isEmpty()) {
            final String sshPrivateKey = Files.readString(Path.of(sshKeyPath), StandardCharsets.UTF_8);
            log.info("Using private key file as credential");
            return Collections.singletonMap("privateKey", sshPrivateKey);
        } else if (password != null && !password.isEmpty()) {
            log.info("Using password as credential");
            return Collections.singletonMap("password", password);
        }
        throw new CredentialNotFoundException("No credential could be established");
    }
}
