package mileschet.bitcoin;

import mileschet.bitcoin.services.WalletService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableWebSecurity
@EnableJpaRepositories
public class SpringBitcoinjApplication {

    @Value("${bitcoin.network}")
    private String network;

    @Bean
    public WalletService addressService() {
        return new WalletService(network);
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBitcoinjApplication.class, args);
    }
}
