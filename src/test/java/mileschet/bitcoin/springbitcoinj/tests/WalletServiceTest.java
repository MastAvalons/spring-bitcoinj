package mileschet.bitcoin.springbitcoinj.tests;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Calendar;

import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import info.blockchain.wallet.bip44.HDWallet;
import info.blockchain.wallet.bip44.HDWalletFactory;
import info.blockchain.wallet.bip44.HDWalletFactory.Language;
import mileschet.bitcoin.springbitcoinj.SpringBitcoinjApplicationTests;

/***
 * 
 * @author programmer
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBitcoinjApplicationTests.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class WalletServiceTest {

	private RestTemplate template = new RestTemplate();

	public byte[] createSeed() {

		// generate secure random seed bip-39
		SecureRandom random = new SecureRandom();
		DeterministicSeed seed = new DeterministicSeed(random, 256, "we are all satoshi!",
				Calendar.getInstance().getTimeInMillis());
		byte[] seedBytes = seed.getSeedBytes();

		return seedBytes;
	}

	@Test
	public void testDeriveAddr() throws MnemonicLengthException, IOException {

		String passphrase = "passphrasetest";
		int mnemonicLength = 12;
		String network = "regtest";

		AbstractBitcoinNetParams networkParameters = MainNetParams.get();
		if (network.equals("testnet")) {
			networkParameters = TestNet3Params.get();
		} else if (network.equals("regtest")) {
			networkParameters = RegTestParams.get();
		}

		// create wallet
		HDWallet wallet = HDWalletFactory.createWallet(networkParameters, Language.US, mnemonicLength, passphrase, 1);

		DeterministicKey key = wallet.getMasterKey();
		System.out.println(wallet.getSeedHex());

		for (int i = 0; i < wallet.getMnemonic().size(); i++) {
			System.out.println("Word " + (i+1) + " : " + wallet.getMnemonic().get(i));
		}

		System.out.println(key.serializePrivB58(networkParameters));
		System.out.println(key.serializePubB58(networkParameters));
		System.out.println(key.getPathAsString());

		int numAddr = Integer.valueOf(1);
		int numAcct = Integer.valueOf(1);

		for (int i = 0; i < numAcct; i++) {

			for (int j = 0; j < numAddr; j++) {

				// external chain
				String addressString = wallet.getAccount(i).getChain(0).getAddressAt(j).getAddressString();
				String path = wallet.getAccount(i).getChain(0).getAddressAt(j).getPath();
				String privateKeyString = wallet.getAccount(i).getChain(0).getAddressAt(j).getPrivateKeyString();

				// change chain
				String addressString2 = wallet.getAccount(i).getChain(1).getAddressAt(j).getAddressString();
				String path2 = wallet.getAccount(i).getChain(1).getAddressAt(j).getPath();
				String privateKeyString2 = wallet.getAccount(i).getChain(1).getAddressAt(j).getPrivateKeyString();

				System.out.println("external addressString : " + addressString);
				System.out.println("external path : " + path);
				System.out.println("external privateKeyString : " + privateKeyString);

				System.out.println("internal addressString : " + addressString2);
				System.out.println("internal path : " + path2);
				System.out.println("internal privateKeyString : " + privateKeyString2);

			}
		}
	}
}
