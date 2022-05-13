package org.hyperledger.fabric.chaincode.assettransfer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class AssetTest {

 @Nested
 class Equality {

  @Test
  public void isReflexive() {
   Asset asset = new Asset("asset1", "Blue", 20, "Guy", 100);
   assertEquals(asset, asset);
  }

  @Test
  public void isSymmetric() {
   Asset assetA = new Asset("asset1", "Blue", 20, "Guy", 100);
   Asset assetB = new Asset("asset1", "Blue", 20, "Guy", 100);

   assertEquals(assetA, assetB);
   assertEquals(assetB, assetA);
  }

  @Test
  public void isTransitive() {
   Asset assetA = new Asset("asset1", "Blue", 20, "Guy", 100);
   Asset assetB = new Asset("asset1", "Blue", 20, "Guy", 100);
   Asset assetC = new Asset("asset1", "Blue", 20, "Guy", 100);

   assertEquals(assetA, assetB);
   assertEquals(assetB, assetC);
   assertEquals(assetA, assetC);
  }

  @Test
  public void handlesInequality() {
   Asset assetA = new Asset("asset1", "Blue", 20, "Guy", 100);
   Asset assetB = new Asset("asset2", "Red", 40, "Lady", 100);

   assertNotEquals(assetA, assetB);
  }

  @Test
  public void handlesOtherObjects() {
   Asset assetA = new Asset("asset1", "Blue", 20, "Guy", 100);
   String assetB = "not a asset";

   assertNotEquals(assetA, assetB);
  }

  @Test
  public void handlesNull() {
   Asset asset = new Asset("asset1", "Blue", 20, "Guy", 100);

   assertNotEquals(asset, null);
  }
 }

 @Test
 public void toStringIdentifiesAsset() {
  Asset asset = new Asset("asset1", "Blue", 20, "Guy", 100);

  assertEquals(asset.toString(), "Asset@e04f6c53 [assetID=asset1, color=Blue, size=20, owner=Guy, appraisedValue=100]");
 }

}
