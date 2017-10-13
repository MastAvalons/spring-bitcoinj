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
import org.bitcoin.protocols.payments.Protos.OutputOrBuilder;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ProtocolException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.Purpose;
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
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.handler.MappedInterceptor;

import com.squareup.okhttp.Response;
import com.subgraph.orchid.encoders.Hex;

import antlr.RecognitionException;
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
		ResponseEntity<String> exchange = null;
		try {
			exchange = template.exchange(uri, HttpMethod.POST, request, String.class);
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

				Transaction tx = new Transaction(networkParameters);
				BigDecimal amount = new BigDecimal(utxo.getJSONObject(i).getString("amount"), MathContext.DECIMAL32);

				exchange = btcJsonRPCRequest("estimatefee", "6");
				JSONObject estimatedFee = new JSONObject(exchange.getBody());
				BigDecimal fee = new BigDecimal(estimatedFee.getString("result"), MathContext.DECIMAL32);
				if (fee.compareTo(BigDecimal.ZERO) == -1) {
					fee = new BigDecimal(0.002, MathContext.DECIMAL64);
				}

				wallet.addAccount();
				String addressTo = wallet.getAccount(1).getChain(0).getAddressAt(0).getAddressString();

				Coin value = Coin.parseCoin(amount.subtract(fee, MathContext.DECIMAL32).toPlainString());
				Address address = Address.fromBase58(networkParameters, addressTo);

				tx.addOutput(value, address);

				String rawTx = resultTx.getString("result");
				Transaction fromTx = new Transaction(networkParameters, Hex.decode(rawTx));
				TransactionOutPoint t = new TransactionOutPoint(networkParameters, 0, fromTx);

				tx.addSignedInput(t, new Script(Hex.decode(utxo.getJSONObject(i).getString("scriptPubKey").getBytes())),
						wallet.getAccount(0).getChain(0).getAddressAt(0).getECKey(), Transaction.SigHash.ALL, true);

				tx.setPurpose(Purpose.USER_PAYMENT);
				byte[] txHex = Hex.encode(tx.unsafeBitcoinSerialize());

				exchange = btcJsonRPCRequest("generate", "1");

				System.out.println("TxHex: " + new String(txHex));

				exchange = btcJsonRPCRequest("sendrawtransaction", "\"" + new String(txHex) + "\", true");

				exchange = btcJsonRPCRequest("generate", "1");

				break;
			}

		}

	}

	public static void main(String[] args) throws ProtocolException, UnsupportedEncodingException {

		String payload = "0100000001371cf7f2df8f06894c0dde5f233a697498e38774e2db36eeb6c898d361139b8c000000006a47304402202f308c8f27738c9c8e60ba43a5d1a477da11870faebacc44a05de50aa8e7691e022074105e1805a9fd1a86b7459e672574f33d9d0662813b7db87c7ede454d6de067812102ea55b3d24eb588f3104b0de2ab0d3bc2aa157e96b91b5fc65582b00bda787241ffffffff0100000000000000001976a914fd17d116b77450a020c67496abf73caaf2fe1bef88ac00000000";
		Transaction tx = new Transaction(RegTestParams.get(), Hex.decode(payload.getBytes()));
		System.out.println(tx.toString());
		System.out.println(tx.getHashAsString());

	}

}
