// For more information on writing tests, see
// https://scalameta.org/munit/docs/getting-started.html
import com.garybake.shoppingbasket.domain._
import munit.FunSuite

class MySuite extends munit.FunSuite {
  test("example test that succeeds") {
    val obtained = 42
    val expected = 42
    assertEquals(obtained, expected)
  }
  
  test("discount calculation works correctly") {
    val apple = Product("A123", "Apples", "bag", 1.00)
    val soup = Product("S123", "Soup", "tin", 0.65)
    val bread = Product("B123", "Bread", "loaf", 0.80)
    
    // Create a basket with 2 apples, 2 soup, 1 bread
    val basket = Basket(Map(apple -> 2, soup -> 2, bread -> 1))
    
    // Create offers
    val appleOffer = PercentageOffSingleProduct(
      product = apple,
      percent = 10,
      label = "Apples 10% off",
      active = true
    )
    
    val soupBreadOffer = BuyNGetMOfOtherAtPercent(
      triggerProduct = soup,
      triggerQty = 2,
      targetProduct = bread,
      targetQtyPerTrigger = 1,
      targetPercentOff = 50,
      label = "Buy 2 Soup, get Bread 50% off",
      active = true
    )
    
    val offerService = OfferService.inMemory(List(appleOffer, soupBreadOffer))
    val discounts = offerService.calculateDiscounts(basket)
    
    // Should have 2 discounts
    assertEquals(discounts.length, 2)
    
    // Apple discount: 2 * £1.00 * 10% = £0.20
    val appleDiscount = discounts.find(_.label.contains("Apples"))
    assert(appleDiscount.isDefined)
    assertEquals(appleDiscount.get.amount, BigDecimal(0.20))
    
    // Bread discount: 1 * £0.80 * 50% = £0.40
    val breadDiscount = discounts.find(_.label.contains("Bread"))
    assert(breadDiscount.isDefined)
    assertEquals(breadDiscount.get.amount, BigDecimal(0.40))
  }
}
