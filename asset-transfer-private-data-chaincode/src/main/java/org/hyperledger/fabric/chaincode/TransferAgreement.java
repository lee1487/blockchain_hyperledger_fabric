package org.hyperledger.fabric.chaincode;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.StandardCharsets;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.json.JSONObject;

@DataType
public final class TransferAgreement {

    @Property
    private final String assetID;
    
    @Property
    private String buyerID;
    
    public String getAssetID() {
        return assetID;
    }
    public String getBuyerID() {
        return buyerID;
    }

    public TransferAgreement(String assetID, String buyerID) {
        this.assetID = assetID;
        this.buyerID = buyerID;
    }
    
    public byte[] serialize() {
        String jsonStr = new JSONObject(this).toString();
        return jsonStr.getBytes(StandardCharsets.UTF_8);
    }
    
    public static TransferAgreement deserialize(final byte[] assetJSON) {
        try {
            JSONObject json = new JSONObject(new String(assetJSON, UTF_8));
            final String id = json.getString("assetID");
            final String buyerID = json.getString("buyerID");
            return new TransferAgreement(id, buyerID);
        } catch (Exception e) {
            throw new ChaincodeException("Deserialize error: " + e.getMessage(), "DATA_ERROR");
        }
    }
    
}
