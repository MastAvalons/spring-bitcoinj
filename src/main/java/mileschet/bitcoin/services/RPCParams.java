package mileschet.bitcoin.services;

public class RPCParams {
    private String rpcuser;
    private String rpcpassword;
    private String rpcport;
    private String rpchost;

    public String getRpcuser() {
        return rpcuser;
    }

    public void setRpcuser(String rpcuser) {
        this.rpcuser = rpcuser;
    }

    public String getRpcpassword() {
        return rpcpassword;
    }

    public void setRpcpassword(String rpcpassword) {
        this.rpcpassword = rpcpassword;
    }

    public String getRpcport() {
        return rpcport;
    }

    public void setRpcport(String rpcport) {
        this.rpcport = rpcport;
    }

    public String getRpchost() {
        return rpchost;
    }

    public void setRpchost(String rpchost) {
        this.rpchost = rpchost;
    }
}
