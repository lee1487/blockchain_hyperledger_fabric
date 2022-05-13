package org.hyperledger.fabric.chaincode.assettransfer;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.annotation.Transaction.TYPE;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(name = "basic", info = @Info(title = "Asset Transfer", description = "The hyperledgendary asset transfer", version = "0.0.1-SNAPSHOT", license = @License(name = "Apache 2.0 License", url = "http://www.apache.org/licenses/LICENSE-2.0.html"), contact = @Contact(email = "a.transfer@example.com", name = "gildong Transfer", url = "https://hyperledger.example.com")))
@Default
public final class AssetTransfer implements ContractInterface {

    private final Genson genson = new Genson();

    private enum AssetTransferErrors {
        ASSET_NOT_FOUND, ASSET_ALREADY_EXISTS
    }

    /**
     * 원장에 초기 자산을 생성
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        createAsset(ctx, "asset1", "blue", 5, "Tomoko", 300);
        createAsset(ctx, "asset2", "red", 5, "Brad", 400);
        createAsset(ctx, "asset3", "green", 10, "Jin Soo", 500);
        createAsset(ctx, "asset4", "yellow", 10, "Max", 600);
        createAsset(ctx, "asset5", "black", 15, "Adrian", 700);
        createAsset(ctx, "asset6", "white", 15, "Michel", 700);
    }

    /**
     * 원장에 새 자산을 생성.
     *
     * @param ctx            the transaction context
     * @param assetID        the ID of the new asset
     * @param color          the color of the new asset
     * @param size           the size for the new asset
     * @param owner          the owner of the new asset
     * @param appraisedValue the appraisedValue of the new asset
     * @return the created asset
     */
    @Transaction(intent = TYPE.SUBMIT)
    public Asset createAsset(final Context ctx, final String assetID, final String color, final int size,
            final String owner, final int appraisedValue) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        if (AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s already exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Asset asset = new Asset(assetID, color, size, owner, appraisedValue);

        // Use Genson to convert the Asset into string, sort it alphabetically and
        // serialize it into a json string
        String sortedJson = genson.serialize(asset);
        stub.putStringState(assetID, sortedJson);
        return asset;
    }

    /**
     * 원장에서 지정된 ID를 가진 자산을 검색한다.
     *
     * @param ctx     the transaction context
     * @param assetID the ID of the asset
     * @return the asset found on the ledger if there was one
     */
    @Transaction(intent = TYPE.EVALUATE)
    public Asset readAsset(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset asset = genson.deserialize(assetJSON, Asset.class);
        return asset;
    }

    /**
     * 원장의 자산 속성을 업데이트한다.
     *
     * @param ctx            the transaction context
     * @param assetID        the ID of the asset being updated
     * @param color          the color of the asset being updated
     * @param size           the size of the asset being updated
     * @param owner          the owner of the asset being updated
     * @param appraisedValue the appraisedValue of the asset being updated
     * @return the transferred asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset updateAsset(final Context ctx, final String assetID, final String color, final int size,
            final String owner, final int appraisedValue) {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset newAsset = new Asset(assetID, color, size, owner, appraisedValue);
        String sortedJson = genson.serialize(newAsset);
        stub.putStringState(assetID, sortedJson);
        return newAsset;
    }

    /**
     * 원장의 자산을 삭제한다.
     *
     * @param ctx     the transaction context
     * @param assetID the ID of the asset being deleted
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void deleteAsset(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }
        stub.delState(assetID);
    }

    /**
     * 원장에 자산이 있는지 확인
     *
     * @param ctx     the transaction context
     * @param assetID the ID of the asset
     * @return boolean indicating the existence of the asset
     */
    @Transaction(intent = TYPE.EVALUATE)
    public boolean AssetExists(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);
        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     * 원장의 자산 소유자를 변경한다.
     *
     * @param ctx      the transaction context
     * @param assetID  the ID of the asset being transferred
     * @param newOwner the new owner
     * @return the old owner
     */
    @Transaction(intent = TYPE.SUBMIT)
    public String TransferAsset(final Context ctx, final String assetID, final String newOwner) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset asset = genson.deserialize(assetJSON, Asset.class);

        Asset newAsset = new Asset(asset.getAssetID(), asset.getColor(), asset.getSize(), asset.getOwner(),
                asset.getAppraisedValue());
        String sortedJson = genson.serialize(newAsset);
        stub.putStringState(assetID, sortedJson);

        return asset.getOwner();
    }

    /**
     * 원장에서 모든 자산을 검색한다.
     *
     * @param ctx the transaction context
     * @return array of assets found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAllAssets(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Asset> queryResults = new ArrayList<Asset>();

        // To retrieve all assets from the ledger use getStateByRange with empty
        // startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning
        // to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive)
        // and asset9 (exclusive) in lexical order
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result : results) {
            Asset asset = genson.deserialize(result.getStringValue(), Asset.class);
            System.out.println(asset);
            queryResults.add(asset);
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

}
