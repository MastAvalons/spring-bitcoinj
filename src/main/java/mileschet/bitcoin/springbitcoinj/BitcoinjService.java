package mileschet.bitcoin.springbitcoinj;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;

import javax.annotation.PostConstruct;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

@Component
public class BitcoinjService {

	public Logger logger = LoggerFactory.getLogger(BitcoinjService.class);

	private WalletAppKit walletAppKit;
	private String seedWords = "RandomSeedWordsBTCJ";
	private static final int SHA256_LENGTH = 32;
	private static final int MAX_PREFIX_LENGTH = 8;
	private static final byte NULL_BYTE = (byte) '\0';

	private static MessageDigest SHA_256 = null;
	static {
		try {
			SHA_256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException nsae) {
			throw new RuntimeException("Programmer error.", nsae);
		}
	}

	@Scheduled(fixedDelay = 30000)
	public void run() {
		if (!isReady()) {
			return;
		}

		logger.info("Receive Address: " + walletAppKit.wallet().currentReceiveAddress());
	}

	@PostConstruct
	public void postConstruct() {
		NetworkParameters params = TestNet3Params.get();

		DeterministicSeed seed = null;
		try {
			seed = new DeterministicSeed(seedWords, null, "", Calendar.getInstance().getTimeInMillis());
		} catch (UnreadableWalletException uwe) {
			throw new RuntimeException(uwe);
		}

		File tempDirectory = new File("/tmp/bitcoinj");
		walletAppKit = new WalletAppKit(params, tempDirectory, ".spv");
		walletAppKit.restoreWalletFromSeed(seed);

		logger.info(seed.toHexString());
		
		start();

		walletAppKit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
			@Override
			public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
				Coin value = tx.getValueSentToMe(w);
				System.out.println("Received tx for " + value.toFriendlyString() + ": " + tx);
				System.out.println("Transaction will be forwarded after it confirms.");
				Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<TransactionConfidence>() {
					@Override
					public void onSuccess(TransactionConfidence result) {
						try {
							makeOP_RETURNTransaction("keep fun in computing");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(Throwable t) {
						// This kind of future can't fail, just rethrow in case
						// something weird happens.
						throw new RuntimeException(t);
					}
				});
			}
		});

	}

	public String makeOP_RETURNTransaction(String message) throws Exception {
		final Wallet wallet = walletAppKit.wallet();
		byte[] hash = SHA_256.digest(message.getBytes());
		String prefix = "RSM";
		byte[] prefixBytes = prefix.getBytes(StandardCharsets.US_ASCII);
		if (MAX_PREFIX_LENGTH < prefix.length()) {
			throw new IllegalArgumentException("OP_RETURN prefix is too long: " + prefix);
		}

		byte[] opReturnValue = new byte[80];
		Arrays.fill(opReturnValue, NULL_BYTE);
		System.arraycopy(prefixBytes, 0, opReturnValue, 0, prefixBytes.length);
		System.arraycopy(hash, 0, opReturnValue, MAX_PREFIX_LENGTH, SHA256_LENGTH);

		Transaction transaction = new Transaction(wallet.getParams());
		transaction.addOutput(Coin.ZERO, ScriptBuilder.createOpReturnScript(hash));

		SendRequest sendRequest = SendRequest.forTx(transaction);

		try {
			wallet.completeTx(sendRequest);
		} catch (InsufficientMoneyException e) {
			throw new Exception("No balance on bitcoin wallet.");
		}

		// Broadcast and commit transaction
		walletAppKit.peerGroup().broadcastTransaction(transaction);
		wallet.commitTx(transaction);

		// Return a reference to the caller
		return transaction.getHashAsString();
	}

	public void start() {
		walletAppKit.setAutoSave(true);
		walletAppKit.setBlockingStartup(true);

		walletAppKit.startAsync();

	}

	public void stop() {
		walletAppKit.stopAsync();
	}

	public boolean isReady() {
		return walletAppKit.isRunning();
	}

	public void waitUntilReady() {
		walletAppKit.awaitRunning();
	}

}
