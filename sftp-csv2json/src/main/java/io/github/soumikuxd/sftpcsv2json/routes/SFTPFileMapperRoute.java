package io.github.soumikuxd.sftpcsv2json.routes;

import io.github.soumikuxd.sftpcsv2json.configurations.BaseConfig;
import io.github.soumikuxd.sftpcsv2json.properties.SFTPSourceProperty;
import io.github.soumikuxd.sftpcsv2json.properties.SFTPTargetProperty;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.CsvDataFormat;
import org.apache.camel.model.dataformat.JsonDataFormat;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
@Component
public class SFTPFileMapperRoute extends RouteBuilder {
    private static final Logger logger = LoggerFactory.getLogger(SFTPFileMapperRoute.class);

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

        CsvDataFormat csv = new CsvDataFormat(this.sftpSourceProperty.getDelimiter());
        JsonDataFormat json = new JsonDataFormat();
        //csv.setSkipHeaderRecord("false");
        csv.setUseMaps("true");

        from(fromSFTPURL.toString())
                .setHeader("appId").simple(this.baseConfig.getAppHeader())
                .setHeader("fileName").simple("${headers:CamelFileName}")
                .log("Started ${headers.CamelFileName}")
                .unmarshal(csv)
                .split(body()).streaming()
                .marshal(json)
                .aggregate(constant(true), (oldExchange, newExchange) -> {
                    if (oldExchange == null) {
                        return newExchange;
                    }

                    String oldBody = oldExchange.getIn().getBody(String.class);
                    String newBody = newExchange.getIn().getBody(String.class);
                    boolean splitComplete = (boolean) newExchange.getProperty("CamelSplitComplete");
                    if (splitComplete) {
                        oldExchange.getIn().setBody(oldBody + System.lineSeparator() + newBody + System.lineSeparator()) ;
                    } else {
                        oldExchange.getIn().setBody(oldBody + System.lineSeparator() + newBody);
                    }
                    oldExchange.setProperty("CamelSplitComplete", newExchange.getProperty("CamelSplitComplete"));
                    return oldExchange;
                })
                .completionPredicate(simple("${exchangeProperty.CamelSplitComplete} == true"))
                .setHeader("fileName").simple("${headers.CamelFileName}" + ".processed")
                .setHeader("CamelFileName").simple("${headers.fileName}")
                .to(toSFTPURL.toString())
                .end()
        ;
    }
}
