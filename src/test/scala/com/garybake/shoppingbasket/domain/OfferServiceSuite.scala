package com.garybake.shoppingbasket.domain

import munit.FunSuite

class OfferServiceSuite extends FunSuite {

  // Test data
  val apple = Product("A123", "Apples", "bag", BigDecimal("1.00"))
  val soup = Product("S123", "Soup", "tin", BigDecimal("0.65"))
  val bread = Product("B123", "Bread", "loaf", BigDecimal("0.80"))
  val milk = Product("M456", "Milk", "bottle", BigDecimal("1.30"))

  test("OfferService.inMemory should return service with provided offers") {
    val offers = List(
      PercentageOffSingleProduct(
        product = apple,
        percent = BigDecimal("15"),
        label = "Apples 15% off",
        active = true
      )
    )

    val service = OfferService.inMemory(offers)

    assert(service.availableOffers == offers)
    assert(service.availableOffers.length == 1)
  }

  test("OfferService.inMemory should handle empty offers list") {
    val service = OfferService.inMemory(List.empty)

    assert(service.availableOffers.isEmpty)
  }

  test("OfferService.inMemory should handle multiple offers") {
    val offers = List(
      PercentageOffSingleProduct(
        product = apple,
        percent = BigDecimal("10"),
        label = "Apples 10% off",
        active = true
      ),
      BuyNGetMOfOtherAtPercent(
        triggerProduct = soup,
        triggerQty = 2,
        targetProduct = bread,
        targetQtyPerTrigger = 1,
        targetPercentOff = BigDecimal("50"),
        label = "Buy 2 Soup, get Bread 50% off",
        active = true
      )
    )

    val service = OfferService.inMemory(offers)

    assert(service.availableOffers.length == 2)
    assert(service.availableOffers.contains(offers.head))
    assert(service.availableOffers.contains(offers(1)))
  }

  test("OfferService.inMemory should return empty discounts for empty basket") {
    val offers = List(
      PercentageOffSingleProduct(
        product = apple,
        percent = BigDecimal("10"),
        label = "Apples 10% off",
        active = true
      )
    )

    val service = OfferService.inMemory(offers)
    val basket = Basket(Map.empty)
    val discounts = service.calculateDiscounts(basket)

    assert(discounts.isEmpty)
  }

  test("OfferService.inMemory should apply single active offer") {
    val offers = List(
      PercentageOffSingleProduct(
        product = apple,
        percent = BigDecimal("20"),
        label = "Apples 20% off",
        active = true
      )
    )

    val service = OfferService.inMemory(offers)
    val basket = Basket(Map(apple -> 2))
    val discounts = service.calculateDiscounts(basket)

    assert(discounts.length == 1)
    assert(discounts.head.label == "Apples 20% off")
    assert(discounts.head.amount == BigDecimal("0.40")) // 20% of (1.00 * 2)
  }

  test("OfferService.inMemory should filter out inactive offers") {
    val offers = List(
      PercentageOffSingleProduct(
        product = apple,
        percent = BigDecimal("10"),
        label = "Active offer",
        active = true
      ),
      PercentageOffSingleProduct(
        product = bread,
        percent = BigDecimal("15"),
        label = "Inactive offer",
        active = false
      )
    )

    val service = OfferService.inMemory(offers)
    val basket = Basket(Map(apple -> 1, bread -> 1))
    val discounts = service.calculateDiscounts(basket)

    assert(discounts.length == 1)
    assert(discounts.head.label == "Active offer")
    assert(!discounts.exists(_.label == "Inactive offer"))
  }

  test("OfferService.inMemory should filter out zero amount discounts") {
    val offers = List(
      PercentageOffSingleProduct(
        product = apple,
        percent = BigDecimal("0"),
        label = "0% discount",
        active = true
      ),
      PercentageOffSingleProduct(
        product = bread,
        percent = BigDecimal("10"),
        label = "10% discount",
        active = true
      )
    )

    val service = OfferService.inMemory(offers)
    val basket = Basket(Map(apple -> 1, bread -> 1))
    val discounts = service.calculateDiscounts(basket)

    assert(discounts.length == 1)
    assert(discounts.head.label == "10% discount")
    assert(!discounts.exists(_.label == "0% discount"))
  }

  test("OfferService.inMemory should apply multiple active offers") {
    val offers = List(
      PercentageOffSingleProduct(
        product = apple,
        percent = BigDecimal("10"),
        label = "Apples 10% off",
        active = true
      ),
      BuyNGetMOfOtherAtPercent(
        triggerProduct = soup,
        triggerQty = 2,
        targetProduct = bread,
        targetQtyPerTrigger = 1,
        targetPercentOff = BigDecimal("50"),
        label = "Buy 2 Soup, get Bread 50% off",
        active = true
      )
    )

    val service = OfferService.inMemory(offers)
    val basket = Basket(
      Map(
        apple -> 2, // 2 * 1.00 * 10% = 0.20 discount
        soup -> 2, // Triggers bread discount
        bread -> 1 // 1 * 0.80 * 50% = 0.40 discount
      )
    )
    val discounts = service.calculateDiscounts(basket)

    assert(discounts.length == 2)
    assert(discounts.exists(_.label == "Apples 10% off"))
    assert(discounts.exists(_.label == "Buy 2 Soup, get Bread 50% off"))

    val totalDiscount = discounts.map(_.amount).sum
    assert(totalDiscount == BigDecimal("0.60")) // 0.20 + 0.40
  }

  test(
    "OfferService.inMemory should handle offers with no applicable products"
  ) {
    val offers = List(
      PercentageOffSingleProduct(
        product = apple,
        percent = BigDecimal("10"),
        label = "Apples 10% off",
        active = true
      )
    )

    val service = OfferService.inMemory(offers)
    val basket = Basket(Map(bread -> 2)) // No apples in basket
    val discounts = service.calculateDiscounts(basket)

    // Apple offer returns 0 discount when no apples, which gets filtered out
    assert(discounts.isEmpty)
  }

  test("OfferService.default should create service with predefined offers") {
    val service = OfferService.default

    assert(service.availableOffers.length == 2)

    val appleOffer = service.availableOffers.find(_.label == "Apples 10% off")
    val soupBreadOffer =
      service.availableOffers.find(_.label == "Buy 2 Soup, get Bread 50% off")

    assert(appleOffer.isDefined)
    assert(soupBreadOffer.isDefined)
    assert(appleOffer.get.active)
    assert(soupBreadOffer.get.active)
  }

  test("OfferService.default should have correct apple offer") {
    val service = OfferService.default
    val appleOffer =
      service.availableOffers.find(_.label == "Apples 10% off").get

    appleOffer match {
      case PercentageOffSingleProduct(product, percent, _, active) =>
        assert(product.sku == "A123")
        assert(product.name == "Apples")
        assert(product.unit == "bag")
        assert(product.unitPrice == BigDecimal("1.00"))
        assert(percent == BigDecimal("10"))
        assert(active)
      case _ => fail("Expected PercentageOffSingleProduct")
    }
  }

  test("OfferService.default should have correct soup-bread offer") {
    val service = OfferService.default
    val soupBreadOffer = service.availableOffers
      .find(_.label == "Buy 2 Soup, get Bread 50% off")
      .get

    soupBreadOffer match {
      case BuyNGetMOfOtherAtPercent(
            triggerProduct,
            triggerQty,
            targetProduct,
            targetQtyPerTrigger,
            targetPercentOff,
            _,
            active
          ) =>
        assert(triggerProduct.sku == "S123")
        assert(triggerProduct.name == "Soup")
        assert(triggerProduct.unit == "tin")
        assert(triggerProduct.unitPrice == BigDecimal("0.65"))
        assert(triggerQty == 2)
        assert(targetProduct.sku == "B123")
        assert(targetProduct.name == "Bread")
        assert(targetProduct.unit == "loaf")
        assert(targetProduct.unitPrice == BigDecimal("0.80"))
        assert(targetQtyPerTrigger == 1)
        assert(targetPercentOff == BigDecimal("50"))
        assert(active)
      case _ => fail("Expected BuyNGetMOfOtherAtPercent")
    }
  }

  test("OfferService.default should calculate discounts correctly") {
    val service = OfferService.default
    val basket = Basket(
      Map(
        apple -> 3, // 3 * 1.00 * 10% = 0.30 discount
        soup -> 4, // 4 soup triggers 2 bread discounts
        bread -> 2 // 2 * 0.80 * 50% = 0.80 discount
      )
    )

    val discounts = service.calculateDiscounts(basket)

    assert(discounts.length == 2)
    assert(discounts.exists(_.label == "Apples 10% off"))
    assert(discounts.exists(_.label == "Buy 2 Soup, get Bread 50% off"))

    val totalDiscount = discounts.map(_.amount).sum
    assert(totalDiscount == BigDecimal("1.10")) // 0.30 + 0.80
  }

  test("OfferService should handle mixed active/inactive offers correctly") {
    val offers = List(
      PercentageOffSingleProduct(
        product = apple,
        percent = BigDecimal("10"),
        label = "Active apple offer",
        active = true
      ),
      PercentageOffSingleProduct(
        product = apple,
        percent = BigDecimal("5"),
        label = "Inactive apple offer",
        active = false
      ),
      PercentageOffSingleProduct(
        product = bread,
        percent = BigDecimal("0"),
        label = "Zero percent offer",
        active = true
      )
    )

    val service = OfferService.inMemory(offers)
    val basket = Basket(Map(apple -> 1, bread -> 1))
    val discounts = service.calculateDiscounts(basket)

    // Only the active apple offer should apply (10% of 1.00 = 0.10)
    // The inactive offer is filtered out
    // The 0% offer returns 0 discount which gets filtered out
    assert(discounts.length == 1)
    assert(discounts.head.label == "Active apple offer")
    assert(discounts.head.amount == BigDecimal("0.10"))
  }
}
