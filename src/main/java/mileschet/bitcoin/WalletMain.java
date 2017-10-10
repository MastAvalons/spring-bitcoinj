package mileschet.bitcoin;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@PropertySource("application-walletmain.properties")
public class WalletMain implements ApplicationRunner {

	public Logger logger = LoggerFactory.getLogger(WalletMain.class);

	public static void main(String[] args) {
		SpringApplication.run(WalletMain.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("Application started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
		logger.info("NonOptionArgs: {}", args.getNonOptionArgs());
		logger.info("OptionNames: {}", args.getOptionNames());

		for (String name : args.getOptionNames()) {
			logger.info("arg-" + name + "=" + args.getOptionValues(name));
		}

		
	}
}
