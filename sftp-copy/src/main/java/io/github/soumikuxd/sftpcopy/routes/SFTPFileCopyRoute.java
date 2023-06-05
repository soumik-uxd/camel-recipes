package io.github.soumikuxd.sftpcopy.routes;

import io.github.soumikuxd.sftpcopy.configurations.BaseConfig;
import io.github.soumikuxd.sftpcopy.properties.SFTPSourceProperty;
import io.github.soumikuxd.sftpcopy.properties.SFTPTargetProperty;
import org.apache.camel.builder.RouteBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

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
        URI fromSFTPURL = new URIBuilder()
                .setScheme("sftp")
                .setHost(this.sftpSourceProperty.getHost())
                .setPort(this.sftpSourceProperty.getPort())
                .setPath(this.sftpSourceProperty.getRemoteDir())
                .addParameter("noop", "true")
                .addParameter("username", this.sftpSourceProperty.getUsername())
                .addParameter("password", this.sftpSourceProperty.getPassword())
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

        URI toSFTPURL = new URIBuilder()
                .setScheme("sftp")
                .setHost(this.sftpTargetProperty.getHost())
                .setPort(this.sftpTargetProperty.getPort())
                .setPath(this.sftpTargetProperty.getRemoteDir())
                .addParameter("username", this.sftpTargetProperty.getUsername())
                .addParameter("password", this.sftpTargetProperty.getPassword())
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
}
