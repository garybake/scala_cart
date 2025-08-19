package com.garybake.shoppingbasket.domain

/** Service for managing offers and calculating applicable discounts.
  *
  * The OfferService provides a centralized way to manage promotional offers and
  * apply them to shopping baskets to calculate total discounts. It handles
  * filtering active offers and aggregating all applicable discounts.
  *
  * @example
  *   {{{
  * // Create a custom offer service
  * val customOffers = List(
  *   PercentageOffSingleProduct(apple, BigDecimal("15"), "Apples 15% off", true)
  * )
  * val service = OfferService.inMemory(customOffers)
  *
  * // Calculate discounts for a basket
  * val basket = Basket(Map(apple -> 2))
  * val discounts = service.calculateDiscounts(basket)
  * // discounts = List(AppliedDiscount("Apples 15% off", 0.18))
  *   }}}
  */
trait OfferService {

  /** Returns all available offers managed by this service.
    *
    * @return
    *   A list of all offers, both active and inactive
    */
  def availableOffers: List[Offer]

  /** Calculates all applicable discounts for a given basket.
    *
    * This method applies all active offers to the basket and returns a
    * consolidated list of discounts. Only offers that result in positive
    * discount amounts are included.
    *
    * @param basket
    *   The shopping basket to calculate discounts for
    * @return
    *   A list of applied discounts, empty if no discounts apply
    *
    * @example
    *   {{{
    * val service = OfferService.default
    * val basket = Basket(Map(
    *   Product("A123", "Apples", "bag", 1.00) -> 3,
    *   Product("S123", "Soup", "tin", 0.65) -> 2,
    *   Product("B123", "Bread", "loaf", 0.80) -> 1
    * ))
    *
    * val discounts = service.calculateDiscounts(basket)
    * // Returns discounts for both:
    * // 1. 10% off apples (3 × $1.00 × 10% = $0.30)
    * // 2. Buy 2 soup, get 1 bread at 50% off ($0.80 × 50% = $0.40)
    *   }}}
    */
  def calculateDiscounts(basket: Basket): List[AppliedDiscount]
}

object OfferService {

  /** Creates a simple in-memory offer service.
    *
    * This factory method creates an OfferService that stores offers in memory
    * and applies them to baskets. The service automatically filters for active
    * offers and only returns discounts with positive amounts.
    *
    * @param offers
    *   The list of offers to manage
    * @return
    *   A new OfferService instance with the specified offers
    *
    * @example
    *   {{{
    * val apple = Product("A123", "Apples", "bag", 1.00)
    * val appleOffer = PercentageOffSingleProduct(
    *   apple, BigDecimal("10"), "Apples 10% off", true
    * )
    *
    * val service = OfferService.inMemory(List(appleOffer))
    * val basket = Basket(Map(apple -> 2))
    * val discounts = service.calculateDiscounts(basket)
    * // discounts = List(AppliedDiscount("Apples 10% off", 0.20))
    *   }}}
    */
  def inMemory(offers: List[Offer]): OfferService = new OfferService {
    override def availableOffers: List[Offer] = offers

    override def calculateDiscounts(basket: Basket): List[AppliedDiscount] = {
      if (basket.isEmpty) return Nil

      // Apply all active offers to the basket
      availableOffers
        .filter(_.active)
        .flatMap(_.applyTo(basket))
        .filter(_.amount > 0)
    }
  }

  /** Creates a default offer service with common promotional rules.
    *
    * This factory method provides a pre-configured OfferService with two common
    * promotional offers:
    *   - 10% discount on apples
    *   - Buy 2 soup, get 1 bread at 50% off
    *
    * The products are created with realistic SKUs, names, and prices suitable
    * for demonstration and testing purposes.
    *
    * @return
    *   A pre-configured OfferService with common promotional offers
    *
    * @example
    *   {{{
    * val service = OfferService.default
    *
    * // Check available offers
    * service.availableOffers.length // 2
    *
    * // Create a basket with items that qualify for discounts
    * val basket = Basket(Map(
    *   Product("A123", "Apples", "bag", 1.00) -> 2,  // Qualifies for 10% off
    *   Product("S123", "Soup", "tin", 0.65) -> 4,   // Qualifies for bread discount (2 triggers)
    *   Product("B123", "Bread", "loaf", 0.80) -> 2  // Can receive bread discount
    * ))
    *
    * val discounts = service.calculateDiscounts(basket)
    * // Returns both applicable discounts:
    * // 1. Apples: 2 × $1.00 × 10% = $0.20
    * // 2. Bread: 2 × $0.80 × 50% = $0.80
    * // Total discount: $1.00
    *   }}}
    */
  def default: OfferService = {
    val apple = Product("A123", "Apples", "bag", 1.00)
    val soup = Product("S123", "Soup", "tin", 0.65)
    val bread = Product("B123", "Bread", "loaf", 0.80)

    val offers = List(
      // 10% off apples
      PercentageOffSingleProduct(
        product = apple,
        percent = 10,
        label = "Apples 10% off",
        active = true
      ),
      // Buy 2 soup, get 1 bread at 50% off
      BuyNGetMOfOtherAtPercent(
        triggerProduct = soup,
        triggerQty = 2,
        targetProduct = bread,
        targetQtyPerTrigger = 1,
        targetPercentOff = 50,
        label = "Buy 2 Soup, get Bread 50% off",
        active = true
      )
    )

    inMemory(offers)
  }
}
