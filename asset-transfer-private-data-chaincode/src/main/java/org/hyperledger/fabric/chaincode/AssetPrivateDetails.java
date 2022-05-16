package org.hyperledger.fabric.chaincode;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.json.JSONObject;

@DataType
public final class AssetPrivateDetails {

    @Property
    private final String assetID;
    @Property
    private int appraisedValue;
    
    public String getAssetID() {
        return assetID;
    }
    
    public int getAppraisedValue() {
        return appraisedValue;
    }

    public AssetPrivateDetails(String assetID, int appraisedValue) {
        this.assetID = assetID;
        this.appraisedValue = appraisedValue;
    }
    
    public byte[] serialize() {
        String jsonStr = new JSONObject(this).toString();
        return jsonStr.getBytes(UTF_8);
    }
    
    public static AssetPrivateDetails deserialize(final byte[] assetJSON) {
        try {
            JSONObject json = new JSONObject(new String(assetJSON, UTF_8));
            final String id = json.getString("assetID");
            final int appraisedValue = json.getInt("appraisedValue");
            
            return new AssetPrivateDetails(id, appraisedValue);
        } catch (Exception e) {
            throw new ChaincodeException("Deserialize error: " + e.getMessage(), "DATA_ERROR");
        }
    }
    
    
}
