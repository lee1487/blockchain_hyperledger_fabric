package org.hyperledger.fabric.chaincode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hyperledger.fabric.chaincode.AssetTransfer.ASSET_COLLECTION_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class AssetTransferTest {

    @Nested
    class InvokeWriteTransaction {
        
        @Test
        public void createAssetWhenAssetExists() {
            //given
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Map<String, byte[]> m = new HashMap<String, byte[]>();
            m.put("asset_properties", dataAsset1Bytes);
            when(ctx.getStub().getTransient()).thenReturn(m);
            when(stub.getPrivateData(ASSET_COLLECTION_NAME, testAsset1ID))
                .thenReturn(dataAsset1Bytes);
            
            //when
            Throwable thrown = catchThrowable(() -> {
               contract.createAsset(ctx); 
            });
            
            //then
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Asset asset1 already exists");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_ALREADY_EXISTS".getBytes());
        }
        
    }
    
    private static String testOrgOneMSP = "TestOrg1";
    private static String testOrg1Client = "testOrg1User";
    
    private static String testAsset1ID = "asset1";
    private static Asset testAsset1 = new Asset("testasset", "asset1", "blue", 5, testOrg1Client);
    private static byte[] dataAsset1Bytes = "{ \"objectType\": \"testasset\", \"assetID\": \"asset1\", \"color\": \"blue\", \"size\": 5, \"owner\": \"testOrg1User\", \"appraisedValue\": 300 }".getBytes();

    

}
