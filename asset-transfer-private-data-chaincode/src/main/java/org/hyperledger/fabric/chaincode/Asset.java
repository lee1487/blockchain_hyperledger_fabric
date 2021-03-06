package org.hyperledger.fabric.chaincode;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.json.JSONObject;

@DataType
public final class Asset {

    @Property
    private final String assetID;
    
    @Property
    private final String objectType;
    
    @Property
    private final String color;
    
    @Property
    private final int size;
    
    @Property
    private String owner;

    public Asset(String objectType, String assetID, String color, int size, String owner) {
        this.objectType = objectType;
        this.assetID = assetID;
        this.color = color;
        this.size = size;
        this.owner = owner;
    }

    public String getAssetID() {
        return assetID;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getColor() {
        return color;
    }

    public int getSize() {
        return size;
    }

    public String getOwner() {
        return owner;
    }
    
    public void setOwner(final String newowner) {
        this.owner = newowner;
    }
    
    public byte[] serialize() {
        String jsonStr = new JSONObject(this).toString();
        return jsonStr.getBytes(UTF_8);
    }
    
    public static Asset deserialize(final byte[] assetJSON) {
        return deserialize(new String(assetJSON, UTF_8));
    }
    
    public static Asset deserialize(final String assetJSON) {
        try {
            JSONObject json = new JSONObject(assetJSON);
            final String id = json.getString("assetID");
            final String type = json.getString("objectType");
            final String color = json.getString("color");
            final String owner = json.getString("owner");
            final int size = json.getInt("size");
            return new Asset(type, id, color, size, owner);
        } catch (Exception e) {
            throw new ChaincodeException("Deserialize error: " + e.getMessage(), "DAT_ERROR");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        
        Asset other = (Asset) obj;
        
        return Objects.deepEquals(
                new String[] {getAssetID(), getColor(), getOwner()},
                new String[] {other.getAssetID(), other.getColor(), other.getOwner()})
                &&
               Objects.deepEquals(
                       new int[] {getSize()}, 
                       new int[] {other.getSize()});
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getObjectType(), getAssetID(), getColor(), getSize(), getOwner());
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode())
        + " [assetID=" + assetID + ", type=" + objectType + ", color="
        + color + ", size=" + size + ", owner=" + owner + "]";
    }
    
}
