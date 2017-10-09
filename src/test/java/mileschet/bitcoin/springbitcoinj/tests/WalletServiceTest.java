package mileschet.bitcoin.springbitcoinj.tests;

import java.security.SecureRandom;
import java.util.Calendar;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.wallet.DeterministicSeed;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import mileschet.bitcoin.springbitcoinj.SpringBitcoinjApplicationTests;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBitcoinjApplicationTests.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class WalletServiceTest {

	private static final int HDW_CHAIN_EXTERNAL = 0;
	private static final int HDW_CHAIN_INTERNAL = 1;
	private NetworkParameters params = RegTestParams.get();

	public byte[] createSeed() {

		// generate secure random seed bip-39
		SecureRandom random = new SecureRandom();
		DeterministicSeed seed = new DeterministicSeed(random, 256, "we are all satoshi!",
				Calendar.getInstance().getTimeInMillis());
		byte[] seedBytes = seed.getSeedBytes();

		return seedBytes;
	}

	@Test
	public void testDeriveAddr() {

		byte[] createSeed = createSeed();
		DeterministicKey ekprv = HDKeyDerivation.createMasterPrivateKey(createSeed);
		
		System.out.println(ekprv.serializePrivB58(params));
		
		DeterministicKey ekpub = HDKeyDerivation.createMasterPubKeyFromBytes(ekprv.getPubKey(), ekprv.getChainCode());

		// Create two accounts
		DeterministicKey ekpub_0 = HDKeyDerivation.deriveChildKey(ekpub, 0);
		DeterministicKey ekpub_1 = HDKeyDerivation.deriveChildKey(ekpub, 1);

		System.out.println(ekpub_0.serializePubB58(params));

		// Create internal and external chain on Account 0
		DeterministicKey ekpub_0_EX = HDKeyDerivation.deriveChildKey(ekpub_0, HDW_CHAIN_EXTERNAL);
		DeterministicKey ekpub_0_IN = HDKeyDerivation.deriveChildKey(ekpub_0, HDW_CHAIN_INTERNAL);

		// Create three addresses on external chain
		DeterministicKey ekpub_0_EX_0 = HDKeyDerivation.deriveChildKey(ekpub_0_EX, 0);
		DeterministicKey ekpub_0_EX_1 = HDKeyDerivation.deriveChildKey(ekpub_0_EX, 1);
		DeterministicKey ekpub_0_EX_2 = HDKeyDerivation.deriveChildKey(ekpub_0_EX, 2);

		System.out.println(ekpub_0_EX_0.serializePubB58(params));
		System.out.println(ekpub_0_EX_1.serializePubB58(params));
		System.out.println(ekpub_0_EX_2.serializePubB58(params));
		System.out.println(ekpub_0_EX_2.toAddress(params));
		// Create three addresses on internal chain
		DeterministicKey ekpub_0_IN_0 = HDKeyDerivation.deriveChildKey(ekpub_0_IN, 0);
		DeterministicKey ekpub_0_IN_1 = HDKeyDerivation.deriveChildKey(ekpub_0_IN, 1);
		DeterministicKey ekpub_0_IN_2 = HDKeyDerivation.deriveChildKey(ekpub_0_IN, 2);

		// Now add a few more addresses with very large indices
		DeterministicKey ekpub_1_IN = HDKeyDerivation.deriveChildKey(ekpub_1, HDW_CHAIN_INTERNAL);
		DeterministicKey ekpub_1_IN_4095 = HDKeyDerivation.deriveChildKey(ekpub_1_IN, 4095);
		System.out.println(ekpub_1_IN_4095.serializePubB58(params));
		// ExtendedHierarchicKey ekpub_1_IN_4bil =
		// HDKeyDerivation.deriveChildKey(ekpub_1_IN, 4294967295L);
	}

}
