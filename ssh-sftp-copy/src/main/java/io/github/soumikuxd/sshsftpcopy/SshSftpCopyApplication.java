package io.github.soumikuxd.sshsftpcopy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@Slf4j
public class SshSftpCopyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SshSftpCopyApplication.class, args);
	}

}
