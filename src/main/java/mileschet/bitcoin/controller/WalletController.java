package mileschet.bitcoin.controller;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import mileschet.bitcoin.repositories.WalletEntity;
import mileschet.bitcoin.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by user on 31/08/17.
 */
@RestController("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @RequestMapping(value = "/{description}", method = RequestMethod.POST)
    @ResponseBody
    public String create(@PathVariable("description") String description, @RequestBody(required = false) String body) throws Exception {

        String entropy = "";
        if (body != null)
            entropy = body;

        // TODO send priv to email or sms
        WalletEntity wallet = walletService.createWallet(entropy);

        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        String result = mapper.writeValueAsString(wallet);

//        wallet.setPriv(null);
//        wallet.setPriv58(null);

        return result;
    }

    @RequestMapping(value = "/{walletId}", method = RequestMethod.GET)
    @ResponseBody
    public String create(@PathVariable("walletId") String walletId) throws Exception {

        WalletEntity wallet = walletService.findWalletById(Long.valueOf(walletId));

        wallet.setPriv58(null);
        wallet.setPriv(null);

        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        String result = mapper.writeValueAsString(wallet);

        return result;
    }


}
