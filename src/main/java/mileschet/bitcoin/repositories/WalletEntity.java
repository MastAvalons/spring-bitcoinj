package mileschet.bitcoin.repositories;


import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class WalletEntity {

    @Id
    private Long id;
    private String priv58;
    private String pub58;
    private byte[] priv;
    private byte[] pub;
    private byte[] seeds;
    private Long creationTime;
    private String entropy;
    private int bits;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPriv58() {
        return priv58;
    }

    public void setPriv58(String priv58) {
        this.priv58 = priv58;
    }

    public String getPub58() {
        return pub58;
    }

    public void setPub58(String pub58) {
        this.pub58 = pub58;
    }

    public byte[] getPriv() {
        return priv;
    }

    public void setPriv(byte[] priv) {
        this.priv = priv;
    }

    public byte[] getPub() {
        return pub;
    }

    public void setPub(byte[] pub) {
        this.pub = pub;
    }

    public byte[] getSeeds() {
        return seeds;
    }

    public void setSeeds(byte[] seeds) {
        this.seeds = seeds;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public String getEntropy() {
        return entropy;
    }

    public void setEntropy(String entropy) {
        this.entropy = entropy;
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
