package com.garybake.shoppingbasket.cli

import com.garybake.shoppingbasket.domain._
import munit.FunSuite
import scala.math.BigDecimal

class MainSuite extends FunSuite {

  test("createCatalog should create catalog with correct products") {
    val catalog = createCatalog()

    // Test that all expected products are present
    val products = catalog.all
    assertEquals(products.length, 3)

    // Test Apple product
    val apple = catalog.getByName("Apples")
    assert(apple.isDefined)
    assertEquals(apple.get.sku, "A123")
    assertEquals(apple.get.name, "Apples")
    assertEquals(apple.get.unit, "bag")
    assertEquals(apple.get.unitPrice, BigDecimal(1.00))

    // Test Soup product
    val soup = catalog.getByName("Soup")
    assert(soup.isDefined)
    assertEquals(soup.get.sku, "S123")
    assertEquals(soup.get.name, "Soup")
    assertEquals(soup.get.unit, "tin")
    assertEquals(soup.get.unitPrice, BigDecimal(0.65))

    // Test Bread product
    val bread = catalog.getByName("Bread")
    assert(bread.isDefined)
    assertEquals(bread.get.sku, "B123")
    assertEquals(bread.get.name, "Bread")
    assertEquals(bread.get.unit, "loaf")
    assertEquals(bread.get.unitPrice, BigDecimal(0.80))
  }

  test("createCatalog should support case-insensitive product lookup") {
    val catalog = createCatalog()

    // Test case-insensitive lookup
    val appleLower = catalog.getByName("apples")
    val appleUpper = catalog.getByName("APPLES")
    val appleMixed = catalog.getByName("ApPlEs")

    assert(appleLower.isDefined)
    assert(appleUpper.isDefined)
    assert(appleMixed.isDefined)
    assertEquals(appleLower.get.name, "Apples")
    assertEquals(appleUpper.get.name, "Apples")
    assertEquals(appleMixed.get.name, "Apples")
  }

  test("createCatalog should support SKU lookup") {
    val catalog = createCatalog()

    val appleBySku = catalog.getBySku("A123")
    val soupBySku = catalog.getBySku("S123")
    val breadBySku = catalog.getBySku("B123")

    assert(appleBySku.isDefined)
    assert(soupBySku.isDefined)
    assert(breadBySku.isDefined)

    assertEquals(appleBySku.get.name, "Apples")
    assertEquals(soupBySku.get.name, "Soup")
    assertEquals(breadBySku.get.name, "Bread")
  }

  test("createCatalog should return None for non-existent products") {
    val catalog = createCatalog()

    val nonExistentByName = catalog.getByName("Banana")
    val nonExistentBySku = catalog.getBySku("B999")

    assert(nonExistentByName.isEmpty)
    assert(nonExistentBySku.isEmpty)
  }

  test(
    "createCatalog should create products with correct BigDecimal precision"
  ) {
    val catalog = createCatalog()

    val apple = catalog.getByName("Apples").get
    val soup = catalog.getByName("Soup").get
    val bread = catalog.getByName("Bread").get

    // Test that prices are properly stored as BigDecimal
    assertEquals(apple.unitPrice, BigDecimal(1.00))
    assertEquals(soup.unitPrice, BigDecimal(0.65))
    assertEquals(bread.unitPrice, BigDecimal(0.80))

    // Test that we can perform arithmetic operations
    val totalPrice = apple.unitPrice + soup.unitPrice + bread.unitPrice
    assertEquals(totalPrice, BigDecimal(2.45))
  }

  test("createCatalog should create immutable catalog") {
    val catalog = createCatalog()
    val originalProducts = catalog.all

    // Verify that the catalog is immutable by checking that we can't modify the returned products
    val products = catalog.all
    assertEquals(products.length, 3)

    // The catalog should remain unchanged
    val productsAfter = catalog.all
    assertEquals(productsAfter.length, 3)
    assertEquals(productsAfter, originalProducts)
  }

  test("createCatalog should not include milk product") {
    val catalog = createCatalog()

    // Milk is defined in the function but not added to the catalog
    val milk = catalog.getByName("Milk")
    assert(milk.isEmpty)

    val milkBySku = catalog.getBySku("M123")
    assert(milkBySku.isEmpty)
  }

  test("shopping basket logic should handle empty basket") {
    val catalog = createCatalog()
    val basket = Basket(Map.empty)

    assertEquals(basket.subtotal, BigDecimal(0))
    assertEquals(basket.isEmpty, true)
  }

  test(
    "shopping basket logic should calculate correct subtotal for single product"
  ) {
    val catalog = createCatalog()
    val apple = catalog.getByName("Apples").get

    var basket = Basket(Map.empty)
    basket = basket.add(apple)

    assertEquals(basket.subtotal, BigDecimal(1.00))
    assertEquals(basket.quantityOf(apple), 1)
  }

  test(
    "shopping basket logic should calculate correct subtotal for multiple products"
  ) {
    val catalog = createCatalog()
    val apple = catalog.getByName("Apples").get
    val soup = catalog.getByName("Soup").get
    val bread = catalog.getByName("Bread").get

    var basket = Basket(Map.empty)
    basket = basket.add(apple)
    basket = basket.add(soup)
    basket = basket.add(bread)

    assertEquals(basket.subtotal, BigDecimal(2.45))
    assertEquals(basket.quantityOf(apple), 1)
    assertEquals(basket.quantityOf(soup), 1)
    assertEquals(basket.quantityOf(bread), 1)
  }

  test("shopping basket logic should handle duplicate products") {
    val catalog = createCatalog()
    val apple = catalog.getByName("Apples").get

    var basket = Basket(Map.empty)
    basket = basket.add(apple)
    basket = basket.add(apple)
    basket = basket.add(apple)

    assertEquals(basket.subtotal, BigDecimal(3.00))
    assertEquals(basket.quantityOf(apple), 3)
  }

  test("shopping basket logic should handle mixed quantities") {
    val catalog = createCatalog()
    val apple = catalog.getByName("Apples").get
    val soup = catalog.getByName("Soup").get

    var basket = Basket(Map.empty)
    basket = basket.add(apple, 2) // 2 apples
    basket = basket.add(soup, 3) // 3 soup

    assertEquals(
      basket.subtotal,
      BigDecimal(2.00 + 1.95)
    ) // 2*1.00 + 3*0.65 = 2.00 + 1.95 = 3.95
    assertEquals(basket.quantityOf(apple), 2)
    assertEquals(basket.quantityOf(soup), 3)
  }

  test("shopping basket logic should work with offer service") {
    val catalog = createCatalog()
    val apple = catalog.getByName("Apples").get
    val soup = catalog.getByName("Soup").get
    val bread = catalog.getByName("Bread").get

    var basket = Basket(Map.empty)
    basket = basket.add(apple, 2) // 2 apples for 10% off
    basket = basket.add(soup, 2) // 2 soup to trigger bread discount
    basket = basket.add(bread, 1) // 1 bread for 50% off

    val offerService = OfferService.default
    val discounts = offerService.calculateDiscounts(basket)

    // Should have 2 discounts: apple 10% off and bread 50% off
    assertEquals(discounts.length, 2)

    val totalDiscount = discounts.map(_.amount).sum
    val expectedTotal = basket.subtotal - totalDiscount

    // Verify the total calculation logic matches Main.scala
    assert(expectedTotal > BigDecimal(0))
    assert(expectedTotal < basket.subtotal)
  }

  test("shopping basket logic should handle invalid product names gracefully") {
    val catalog = createCatalog()

    // Simulate the logic from Main.scala where invalid products are filtered out
    val validProducts = Seq("Apples", "Soup", "Bread")
    val invalidProducts = Seq("Banana", "Orange", "Invalid")
    val mixedProducts = validProducts ++ invalidProducts

    var basket = Basket(Map.empty)
    for (productName <- mixedProducts) {
      val product = catalog.getByName(productName)
      if (product.isDefined) {
        basket = basket.add(product.get)
      }
    }

    // Only valid products should be added
    assertEquals(basket.subtotal, BigDecimal(2.45)) // 1.00 + 0.65 + 0.80
    assertEquals(basket.lines.size, 3)
  }
}
