package mileschet.bitcoin.controller;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import mileschet.bitcoin.repositories.WalletEntity;
import mileschet.bitcoin.services.WalletService;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by user on 31/08/17.
 */
@RestController("/")
public class WalletController {

	@Autowired
	private WalletService walletService;

	@RequestMapping(value = "/wallet", method = RequestMethod.POST)
	@ResponseBody
	public String create(@RequestBody(required = false) String body) throws Exception {

		String entropy = "";
		String customerId = "";
		if (body != null) {
			JSONObject a = new JSONObject(body);
			entropy = a.optString("entropy");
			customerId = a.optString("customerId");
		}

		// TODO send priv to email or sms
		WalletEntity wallet = walletService.createWallet(entropy, customerId);

		ObjectMapper mapper = new ObjectMapper(new JsonFactory());
		String result = mapper.writeValueAsString(wallet);

		// wallet.setPriv(null);
		// wallet.setPriv58(null);

		return result;
	}

	@RequestMapping(value = "/wallet/{walletId}", method = RequestMethod.GET)
	@ResponseBody
	public String get(@PathVariable("walletId") String walletId) throws Exception {

		WalletEntity wallet = walletService.findWalletById(Long.valueOf(walletId));

		wallet.setPriv58(null);
		wallet.setPriv(null);

		ObjectMapper mapper = new ObjectMapper(new JsonFactory());
		String result = mapper.writeValueAsString(wallet);

		return result;
	}

	@RequestMapping(value = "/wallet/addr/{walletId}", method = RequestMethod.POST)
	@ResponseBody
	public String newAddr(@PathVariable("walletId") String walletId, @RequestBody String body) throws Exception {

		WalletEntity wallet = walletService.findWalletById(Long.valueOf(walletId));
		
		walletService.generatePubAddr(wallet);
		
		ObjectMapper mapper = new ObjectMapper(new JsonFactory());
		String result = mapper.writeValueAsString(wallet);

		return result;
	}

}
