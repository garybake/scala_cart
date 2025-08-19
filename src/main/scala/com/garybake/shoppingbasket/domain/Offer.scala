package com.garybake.shoppingbasket.domain

/** Contract for any promotional rule that can be applied to a shopping basket.
  *
  * Offers represent various types of discounts and promotions that can be
  * applied to baskets to reduce the total cost. Each offer type implements its
  * own logic for calculating applicable discounts based on the basket contents.
  *
  * @example
  *   {{{
  * // Create a 10% off offer for apples
  * val appleOffer = PercentageOffSingleProduct(
  *   Product("APPLE", "apple", BigDecimal("0.60")),
  *   BigDecimal("10"),
  *   "Apples 10% off",
  *   true
  * )
  *
  * // Create a buy-2-get-1-at-50%-off offer
  * val soupBreadOffer = BuyNGetMOfOtherAtPercent(
  *   Product("SOUP", "soup", BigDecimal("0.65")), // trigger product
  *   2,                                          // buy 2
  *   Product("BREAD", "bread", BigDecimal("0.80")), // target product
  *   1,                                          // get 1
  *   BigDecimal("50"),                           // at 50% off
  *   "Buy 2 Soup, get 1 Bread at 50% off",
  *   true
  * )
  *   }}}
  */
sealed trait Offer {

  /** A human-readable description of the offer for display purposes.
    *
    * @return
    *   A string describing the offer (e.g., "Apples 10% off")
    */
  def label: String

  /** Whether this offer is currently active and should be applied.
    *
    * @return
    *   true if the offer should be applied, false otherwise
    */
  def active: Boolean

  /** Applies the offer to a basket and returns any applicable discounts.
    *
    * @param basket
    *   The shopping basket to apply the offer to
    * @return
    *   A list of applied discounts (empty if no discounts apply)
    */
  def applyTo(basket: Basket): List[AppliedDiscount]
}

/** A percentage discount applied to a single product type.
  *
  * This offer reduces the price of a specific product by a given percentage for
  * all quantities of that product in the basket.
  *
  * @param product
  *   The product to apply the discount to
  * @param percent
  *   The percentage discount (e.g., 10 for 10% off)
  * @param label
  *   A human-readable description of the offer
  * @param active
  *   Whether this offer is currently active
  *
  * @example
  *   {{{
  * val apple = Product("APPLE", "apple", BigDecimal("0.60"))
  * val offer = PercentageOffSingleProduct(apple, BigDecimal("10"), "Apples 10% off", true)
  *
  * val basket = Basket(Map(apple -> 3)) // 3 apples at $0.60 each = $1.80
  * val discounts = offer.applyTo(basket)
  * // discounts = List(AppliedDiscount("Apples 10% off", 0.18))
  * // Total discount: $0.18 (10% of $1.80)
  *   }}}
  */
final case class PercentageOffSingleProduct(
    product: Product,
    percent: BigDecimal, // e.g., 10 for 10%
    label: String,
    active: Boolean
) extends Offer {
  override def applyTo(basket: Basket): List[AppliedDiscount] = {
    val quantity = basket.quantityOf(product)
    val discount = product.unitPrice * percent / 100 * quantity
    List(AppliedDiscount(label, discount))
  }
}

/** A conditional discount where buying N of one product triggers a discount on
  * another product.
  *
  * This offer implements scenarios like "Buy 2 Soup, get 1 Bread at 50% off".
  * The discount is calculated based on how many trigger items are purchased and
  * how many target items are available for discounting.
  *
  * @param triggerProduct
  *   The product that must be purchased to trigger the offer
  * @param triggerQty
  *   The quantity of trigger product required (N in "Buy N")
  * @param targetProduct
  *   The product that receives the discount
  * @param targetQtyPerTrigger
  *   How many target items get discounted per trigger (M in "get M")
  * @param targetPercentOff
  *   The percentage discount on the target product (e.g., 50 for 50% off)
  * @param label
  *   A human-readable description of the offer
  * @param active
  *   Whether this offer is currently active
  *
  * @example
  *   {{{
  * val soup = Product("SOUP", "soup", BigDecimal("0.65"))
  * val bread = Product("BREAD", "bread", BigDecimal("0.80"))
  *
  * val offer = BuyNGetMOfOtherAtPercent(
  *   triggerProduct = soup,
  *   triggerQty = 2,           // Buy 2
  *   targetProduct = bread,
  *   targetQtyPerTrigger = 1,  // Get 1
  *   targetPercentOff = BigDecimal("50"), // At 50% off
  *   label = "Buy 2 Soup, get 1 Bread at 50% off",
  *   active = true
  * )
  *
  * val basket = Basket(Map(soup -> 4, bread -> 2)) // 4 soup, 2 bread
  * val discounts = offer.applyTo(basket)
  * // 4 soup ÷ 2 = 2 triggers, so 2 bread items get 50% off
  * // 2 × $0.80 × 50% = $0.80 total discount
  * // discounts = List(AppliedDiscount("Buy 2 Soup, get 1 Bread at 50% off", 0.80))
  *   }}}
  *
  * @note
  *   The discount is limited by the actual quantity of target product in the
  *   basket. If you buy 4 soup but only have 1 bread, only 1 bread gets
  *   discounted.
  */
final case class BuyNGetMOfOtherAtPercent(
    triggerProduct: Product,
    triggerQty: Int, // N
    targetProduct: Product,
    targetQtyPerTrigger: Int, // M (use 1 for the given brief)
    targetPercentOff: BigDecimal, // e.g., 50
    label: String,
    active: Boolean
) extends Offer {
  override def applyTo(basket: Basket): List[AppliedDiscount] = {
    val triggerQty = basket.quantityOf(triggerProduct)
    val targetQty = basket.quantityOf(targetProduct)

    // Calculate how many target items can get the discount
    val eligibleTargetQty = (triggerQty / this.triggerQty) * targetQtyPerTrigger

    // Don't give more discounts than target items in basket
    val actualDiscountedQty = Math.min(eligibleTargetQty, targetQty)

    val discount =
      targetProduct.unitPrice * targetPercentOff / 100 * actualDiscountedQty

    if (discount > 0 && eligibleTargetQty > 0) {
      List(AppliedDiscount(label, discount))
    } else {
      Nil
    }
  }
}
