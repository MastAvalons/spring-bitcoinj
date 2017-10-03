package mileschet.bitcoin;

import mileschet.bitcoin.services.RPCParams;
import mileschet.bitcoin.services.WalletService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableWebSecurity
@EnableJpaRepositories
@ComponentScan
public class SpringBitcoinjApplication {

    @Value("${bitcoin.network}")
    private String network;
    @Value("${bitcoin.rpcuser}")
    private String rpcuser;
    @Value("${bitcoin.rpcpassword}")
    private String rpcpassword;
    @Value("${bitcoin.rpcport}")
    private String rpcport;
    @Value("${bitcoin.rpchost}")
    private String rpchost;

    @Bean
    public WalletService walletService() {
        RPCParams params = new RPCParams(rpcuser, rpcpassword, rpcport, rpchost);
        return new WalletService(network, params);
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBitcoinjApplication.class, args);
    }
}
