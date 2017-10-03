package mileschet.bitcoin.services;

import com.azazar.bitcoin.jsonrpcclient.BitcoinJSONRPCClient;
import mileschet.bitcoin.repositories.WalletEntity;
import mileschet.bitcoin.repositories.WalletEntityRepository;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.List;

@Service
public class WalletService {
    public Logger logger = LoggerFactory.getLogger(WalletService.class);

    private String network;
    private RPCParams rpcparams;

    @Autowired
    private WalletEntityRepository walletRepository;

    public WalletService() {
    }

    public WalletService(String network, RPCParams rpcParams) {
        this.network = network;
        this.rpcparams = rpcParams;
    }

    public WalletEntity createWallet(String entropy) {
        NetworkParameters params = MainNetParams.get();
        if (network.equals("testnet")) {
            params = TestNet3Params.get();
        }

//        String entropy = "";
//        try {
//            BufferedReader br = new BufferedReader(new InputStreamReader((System.in)));
//            System.out.println("Please enter the entropy to generate your hd wallet: ");
//            entropy = br.readLine();
//        } catch (IOException e) {
//        }

        Long creationtime = Calendar.getInstance().getTimeInMillis();

        try {
            // generate secure random seed bip-39
            SecureRandom random = new SecureRandom();
            DeterministicSeed seed = new DeterministicSeed(random, 256, entropy, creationtime);
            byte[] seedBytes = seed.getSeedBytes();

            // bip 32 generate hd wallet keys
            DeterministicKey hd = HDKeyDerivation.createMasterPrivateKey(seedBytes);
            String priv58 = hd.serializePrivB58(params);
            String pub58 = hd.serializePubB58(params);

            WalletEntity wallet = new WalletEntity();
            wallet.setBits(256);
            wallet.setCreationTime(creationtime);
            wallet.setEntropy(entropy);
            wallet.setPriv(hd.getPrivKeyBytes());
            wallet.setPub(hd.getPubKey());
            wallet.setPub58(pub58);
            wallet.setPriv58(priv58);

            wallet = walletRepository.save(wallet);

            return wallet;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }

    }

    public WalletEntity findWalletById(Long id) {
        WalletEntity oneById = walletRepository.findOneById(id);
        return oneById;
    }

    public void getBalance(String key) throws MalformedURLException {
        BitcoinJSONRPCClient rpc = new BitcoinJSONRPCClient("http://" + rpcparams.getRpcuser() + ':' + rpcparams.getRpcpassword() + "@" + rpcparams.getRpchost() + ":" + rpcparams.getRpcport());

    }
}
