package mileschet.bitcoin.services;

import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.util.Calendar;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mileschet.bitcoin.repositories.WalletEntity;
import mileschet.bitcoin.repositories.WalletEntityRepository;

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

	public WalletEntity createWallet(String entropy, String customerId) {
		NetworkParameters params = MainNetParams.get();
		if (network.equals("testnet")) {
			params = TestNet3Params.get();
		} else if (network.equals("regnet")) {
			params = RegTestParams.get();
		}


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
			wallet.setDescription(customerId);

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
		// BitcoinJSONRPCClient rpc = new BitcoinJSONRPCClient("http://" +
		// rpcparams.getRpcuser() + ':' + rpcparams.getRpcpassword() + "@" +
		// rpcparams.getRpchost() + ":" + rpcparams.getRpcport());

	}
	
	private static final int HDW_CHAIN_EXTERNAL = 0;
    private static final int HDW_CHAIN_INTERNAL = 1;

	public void generatePubAddr(WalletEntity wallet) {
		DeterministicKey ekprv = HDKeyDerivation.createMasterPrivateKey(wallet.getSeeds());
		
        DeterministicKey ekpub = HDKeyDerivation.createMasterPubKeyFromBytes(wallet.getPub(), ekprv.getChainCode());

        // Create two accounts
        DeterministicKey ekpub_0 = HDKeyDerivation.deriveChildKey(ekpub, 0);
        DeterministicKey ekpub_1 = HDKeyDerivation.deriveChildKey(ekpub, 1);
        
        logger.info(ekpub_0.toString());
        logger.info(ekpub_1.toString());
        
        // Create internal and external chain on Account 0
        DeterministicKey ekpub_0_EX = HDKeyDerivation.deriveChildKey(ekpub_0, HDW_CHAIN_EXTERNAL);
        DeterministicKey ekpub_0_IN = HDKeyDerivation.deriveChildKey(ekpub_0, HDW_CHAIN_INTERNAL);

        // Create three addresses on external chain
        DeterministicKey ekpub_0_EX_0 = HDKeyDerivation.deriveChildKey(ekpub_0_EX, 0);
        DeterministicKey ekpub_0_EX_1 = HDKeyDerivation.deriveChildKey(ekpub_0_EX, 1);
        DeterministicKey ekpub_0_EX_2 = HDKeyDerivation.deriveChildKey(ekpub_0_EX, 2);

        // Create three addresses on internal chain
        DeterministicKey ekpub_0_IN_0 = HDKeyDerivation.deriveChildKey(ekpub_0_IN, 0);
        DeterministicKey ekpub_0_IN_1 = HDKeyDerivation.deriveChildKey(ekpub_0_IN, 1);
        DeterministicKey ekpub_0_IN_2 = HDKeyDerivation.deriveChildKey(ekpub_0_IN, 2);

        // Now add a few more addresses with very large indices
        DeterministicKey ekpub_1_IN = HDKeyDerivation.deriveChildKey(ekpub_1, HDW_CHAIN_INTERNAL);
        DeterministicKey ekpub_1_IN_4095 = HDKeyDerivation.deriveChildKey(ekpub_1_IN, 4095);
//        ExtendedHierarchicKey ekpub_1_IN_4bil = HDKeyDerivation.deriveChildKey(ekpub_1_IN, 4294967295L);

	}
}
