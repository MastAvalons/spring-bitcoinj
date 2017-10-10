package mileschet.bitcoin.springbitcoinj.tests;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Calendar;

import org.apache.commons.codec.DecoderException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.bitcoin.protocols.payments.Protos.OutputOrBuilder;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.crypto.MnemonicException.MnemonicWordException;
import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import info.blockchain.wallet.bip44.HDChain;
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

	public void testCreateWallet() {
		// create wallet
		// HDWallet wallet = HDWalletFactory.createWallet(networkParameters,
		// Language.US, mnemonicLength, passphrase, 1);
	}

	public HttpHeaders createHeaders(String username, String password) {
		return new HttpHeaders() {
			{
				String auth = username + ":" + password;
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);
				set("Authorization", authHeader);
			}
		};
	}

	@Test
	public void testJSONRPCAndSendToAddr() throws MnemonicLengthException, IOException, AddressFormatException,
			MnemonicWordException, MnemonicChecksumException, DecoderException {

		String passphrase = "passphrasetest";
		int mnemonicLength = 12;
		String network = "regtest";

		AbstractBitcoinNetParams networkParameters = MainNetParams.get();
		if (network.equals("testnet")) {
			networkParameters = TestNet3Params.get();
		} else if (network.equals("regtest")) {
			networkParameters = RegTestParams.get();
		}

		String words = "promote neither chalk mystery among negative wood afford drip flash tiny enforce";
		HDWallet wallet = HDWalletFactory.restoreWallet(networkParameters, Language.US, words, passphrase, 1);

		// create a transaction using json-rpc

		ResponseEntity<String> exchange = btcJsonRPCRequest("getblockchaininfo");
		System.out.println(exchange.getBody());

		exchange = btcJsonRPCRequest("generate", "1");
		System.out.println(exchange.getBody());

		String addr = "\"" + wallet.getAccount(0).getChain(0).getAddressAt(0).getAddressString() + "\"";
		exchange = btcJsonRPCRequest("importaddress", addr);
		System.out.println(exchange.getBody());

		String payload = addr + ", \"1\"";
		exchange = btcJsonRPCRequest("sendtoaddress", payload);
		System.out.println(exchange.getBody());

		exchange = btcJsonRPCRequest("generate", "1");
		System.out.println(exchange.getBody());

		payload = addr + ", 1";
		exchange = btcJsonRPCRequest("getreceivedbyaddress", payload);
		System.out.println(exchange.getBody());

		// create a transaction bitcoinj

	}

	public ResponseEntity<String> btcJsonRPCRequest(String method) {
		return btcJsonRPCRequest(method, null);
	}

	public ResponseEntity<String> btcJsonRPCRequest(String method, String params) {
		String rpcuser = "Ulysseys";
		String rpcpassword = "password";
		String uri = "http://127.0.0.1:18332/";

		if (params == null) {
			params = "";
		}

		String message = "{\"id\":\"t0\", \"method\": \"" + method + "\", \"params\": [ " + params + "] }";
		HttpEntity<String> request = new HttpEntity<String>(message, createHeaders(rpcuser, rpcpassword));
		ResponseEntity<String> exchange = template.exchange(uri, HttpMethod.POST, request, String.class);
		return exchange;
	}

	@Test
	public void testDeriveAddr() throws MnemonicLengthException, IOException, AddressFormatException,
			MnemonicWordException, MnemonicChecksumException, DecoderException {
		String passphrase = "passphrasetest";
		int mnemonicLength = 12;
		String network = "regtest";

		AbstractBitcoinNetParams networkParameters = MainNetParams.get();
		if (network.equals("testnet")) {
			networkParameters = TestNet3Params.get();
		} else if (network.equals("regtest")) {
			networkParameters = RegTestParams.get();
		}

		String words = "promote neither chalk mystery among negative wood afford drip flash tiny enforce";
		HDWallet wallet = HDWalletFactory.restoreWallet(networkParameters, Language.US, words, passphrase, 1);

		DeterministicKey key = wallet.getMasterKey();
		System.out.println(wallet.getSeedHex());

		// for (int i = 0; i < wallet.getMnemonic().size(); i++) {
		// System.out.println("Word " + (i + 1) + " : " + wallet.getMnemonic().get(i));
		// }
		int numAddr = Integer.valueOf(1);
		int numAcct = Integer.valueOf(1);

		for (int i = 0; i < numAcct; i++) {

			for (int j = 0; j < numAddr; j++) {

				HDChain external = wallet.getAccount(i).getChain(0);

			}
		}
	}

	@Test
	public void testJSONRPCAndCreateTransaction() throws MnemonicLengthException, IOException, AddressFormatException,
			MnemonicWordException, MnemonicChecksumException, DecoderException, JSONException {

		String passphrase = "passphrasetest";
		int mnemonicLength = 12;
		String network = "regtest";

		AbstractBitcoinNetParams networkParameters = MainNetParams.get();
		if (network.equals("testnet")) {
			networkParameters = TestNet3Params.get();
		} else if (network.equals("regtest")) {
			networkParameters = RegTestParams.get();
		}

		String words = "promote neither chalk mystery among negative wood afford drip flash tiny enforce";
		HDWallet wallet = HDWalletFactory.restoreWallet(networkParameters, Language.US, words, passphrase, 1);

		// create a transaction using json-rpc

		ResponseEntity<String> exchange = btcJsonRPCRequest("getblockchaininfo");
		System.out.println(exchange.getBody());

		exchange = btcJsonRPCRequest("generate", "1");
		System.out.println(exchange.getBody());

		String addr = "\"" + wallet.getAccount(0).getChain(0).getAddressAt(0).getAddressString() + "\"";
		String payload = "1, 9999999, [ " + addr + "]";
		exchange = btcJsonRPCRequest("listunspent", payload);
		System.out.println(exchange.getBody());

		String addressStr = wallet.getAccount(0).getChain(0).getAddressAt(0).getAddressString(););
		
		JSONObject json = new JSONObject(exchange.getBody());
		JSONArray utxo = json.getJSONArray("result");
		for (int i = 0; i < utxo.length(); i++) {

			if (utxo.getJSONObject(i).optString("address")
					.equals(addressStr)) {
				
				System.out.println(utxo);
				
				Coin value = Coin.parseCoin("0.10");
				Address address = Address.fromBase58(networkParameters, addressStr);
				
				Transaction tx = new Transaction(networkParameters);
				tx.addOutput(value, address);
//				TransactionOutPoint t = new TransactionOutPoint(networkParameters, )
				
				break;
			}

		}

	}
}
