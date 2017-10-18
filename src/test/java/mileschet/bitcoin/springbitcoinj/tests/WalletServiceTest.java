package mileschet.bitcoin.springbitcoinj.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Calendar;

import org.apache.commons.codec.DecoderException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ProtocolException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.Purpose;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException.MnemonicChecksumException;
import org.bitcoinj.crypto.MnemonicException.MnemonicLengthException;
import org.bitcoinj.crypto.MnemonicException.MnemonicWordException;
import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.DeterministicSeed;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.subgraph.orchid.encoders.Hex;

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

	public Logger logger = LoggerFactory.getLogger(WalletServiceTest.class);

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

		String message = "{\"id\":\"t0\", \"method\": \"" + method + "\", \"params\": [ " + params + " ] }";
		logger.info("Request: " + message);
		HttpEntity<String> request = new HttpEntity<String>(message, createHeaders(rpcuser, rpcpassword));
		ResponseEntity<String> exchange = null;
		try {
			exchange = template.exchange(uri, HttpMethod.POST, request, String.class);
			logger.info("Response: " + exchange.getBody());
			return exchange;
		} catch (HttpServerErrorException ex) {
			System.out.println("RPC Request Error Response: " + ex.getResponseBodyAsString());
			ex.printStackTrace();
			throw ex;
		}

	}

	@Test
	public void testDeriveAddr() throws MnemonicLengthException, IOException, AddressFormatException,
			MnemonicWordException, MnemonicChecksumException, DecoderException {
		String passphrase = "passphrasetest";
		String network = "regtest";

		AbstractBitcoinNetParams networkParameters = MainNetParams.get();
		if (network.equals("testnet")) {
			networkParameters = TestNet3Params.get();
		} else if (network.equals("regtest")) {
			networkParameters = RegTestParams.get();
		}

		String words = "promote neither chalk mystery among negative wood afford drip flash tiny enforce";
		HDWallet wallet = HDWalletFactory.restoreWallet(networkParameters, Language.US, words, passphrase, 1);

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
		exchange = btcJsonRPCRequest("generate", "200");

		String addr = "\"" + wallet.getAccount(0).getChain(0).getAddressAt(0).getAddressString() + "\"";
		exchange = btcJsonRPCRequest("importaddress", addr);

		exchange = btcJsonRPCRequest("generate", "5");

		String payload = addr + ", \"1\"";
		exchange = btcJsonRPCRequest("sendtoaddress", payload);

		exchange = btcJsonRPCRequest("generate", "10");

		payload = "1, 9999999, [ " + addr + "]";
		exchange = btcJsonRPCRequest("listunspent", payload);

		String addressStr = wallet.getAccount(0).getChain(0).getAddressAt(0).getAddressString();

		JSONObject json = new JSONObject(exchange.getBody());
		JSONArray utxo = json.getJSONArray("result");
		for (int i = 0; i < utxo.length(); i++) {

			// only transaction from an address
			if (utxo.getJSONObject(i).optString("address").equals(addressStr)) {

				String txid = "\"" + utxo.getJSONObject(i).getString("txid") + "\"";
				exchange = btcJsonRPCRequest("getrawtransaction", txid);
				JSONObject resultTx = new JSONObject(exchange.getBody());

				exchange = btcJsonRPCRequest("importaddress", addr);
				exchange = btcJsonRPCRequest("generate", "5");
				exchange = btcJsonRPCRequest("estimatefee", "6");
				JSONObject estimatedFee = new JSONObject(exchange.getBody());
				BigDecimal fee = new BigDecimal(estimatedFee.getString("result"), MathContext.DECIMAL32);
				if (fee.compareTo(BigDecimal.ZERO) == -1) {
					fee = new BigDecimal(0.000038, MathContext.DECIMAL32);
				}

				wallet.addAccount();
				String addressTo = wallet.getAccount(1).getChain(0).getAddressAt(0).getAddressString();

				Transaction tx = new Transaction(networkParameters);
				tx.setPurpose(Purpose.USER_PAYMENT);

				BigDecimal amount = new BigDecimal(utxo.getJSONObject(i).getString("amount"), MathContext.DECIMAL32);
				BigDecimal half = amount.divide(BigDecimal.valueOf(2L), MathContext.DECIMAL32);
				Coin value = Coin.parseCoin(half.subtract(fee, MathContext.DECIMAL32).toPlainString());

				String rawTx = resultTx.getString("result");
				Transaction fromTx = new Transaction(networkParameters, Hex.decode(rawTx));
				TransactionOutPoint t = new TransactionOutPoint(networkParameters, 1, fromTx.getHash());

				ECKey cKey = wallet.getAccount(0).getChain(0).getAddressAt(0).getECKey();

				logger.info(utxo.getJSONObject(i).getString("scriptPubKey"));
				// byte[] uxtoScript =
				// Hex.decode(utxo.getJSONObject(i).getString("scriptPubKey"));
				Script script = new Script(Hex.decode(utxo.getJSONObject(i).getString("scriptPubKey").getBytes()));

				Address address = Address.fromBase58(networkParameters, addressTo);
				Address changeAddr = wallet.getAccount(0).getChain(1).getAddressAt(0).getAddress();

				exchange = btcJsonRPCRequest("importaddress", "\"" + address.toBase58() + "\"");
				exchange = btcJsonRPCRequest("importaddress", "\"" + changeAddr.toBase58() + "\"");

				tx.addOutput(value, address);
				tx.addOutput(Coin.parseCoin(half.toPlainString()), changeAddr);
				tx.addSignedInput(t, script, cKey);

				byte[] txHex = Hex.encode(tx.unsafeBitcoinSerialize());
				String strTx = new String(txHex);
				System.out.println("TxHex: " + strTx);

				exchange = btcJsonRPCRequest("generate", "1");
				exchange = btcJsonRPCRequest("sendrawtransaction", "\"" + strTx + "\", true");
				exchange = btcJsonRPCRequest("generate", "10");

				break;
			}

		}

	}

	public static void main(String[] args) throws ProtocolException, UnsupportedEncodingException {

		String payload = "010000000101973e9e727e21f35be8a791dbd65d96e779df6e2e13d73bd0e593e55db1359d000000006a4730440220738764060d70335003ce9817c1a7e2648011062733e52587fa3ab5e9b81e72f402201a6063b5341cce7d52819364347b4fa2c7c18bc7d613700a2a0954f8eadad2d98121024a1728b77e360c402df95e0ad5faac8aa5d85bb4e620598b30b474f29cf07259ffffffff02a8e1fa02000000001976a914fd17d116b77450a020c67496abf73caaf2fe1bef88ac80f0fa02000000001976a91444f2334f6b116235f082fe4e08e606c7191de70888ac00000000	";
		Transaction tx = new Transaction(RegTestParams.get(), Hex.decode(payload.getBytes()));
		System.out.println(tx.toString());
		System.out.println(tx.getHashAsString());

	}

}
