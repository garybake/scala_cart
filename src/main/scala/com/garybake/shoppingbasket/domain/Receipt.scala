package com.garybake.shoppingbasket.domain

/** Represents the final pricing result for a shopping basket.
  *
  * A Receipt contains the complete financial summary of a shopping transaction,
  * including the original subtotal, all applied discounts, and the final total
  * after discounts. This serves as the output of the pricing calculation
  * process and can be used for display, printing, or further processing.
  *
  * @param subtotal
  *   The original basket total before any discounts are applied
  * @param discounts
  *   A list of all discounts that were applied to the basket
  * @param total
  *   The final amount to be paid after all discounts
  *
  * @example
  *   {{{
  * // Create a receipt for a basket with discounts
  * val apple = Product("A123", "Apples", "bag", BigDecimal("1.00"))
  * val soup = Product("S456", "Soup", "tin", BigDecimal("0.65"))
  * val bread = Product("B789", "Bread", "loaf", BigDecimal("0.80"))
  *
  * val basket = Basket(Map(
  *   apple -> 3,   // 3 × $1.00 = $3.00
  *   soup -> 2,    // 2 × $0.65 = $1.30
  *   bread -> 1    // 1 × $0.80 = $0.80
  * ))
  *
  * val subtotal = basket.subtotal // $5.10
  * val discounts = List(
  *   AppliedDiscount("Apples 10% off", BigDecimal("0.30")),      // 10% of $3.00
  *   AppliedDiscount("Buy 2 Soup, get Bread 50% off", BigDecimal("0.40")) // 50% of $0.80
  * )
  * val total = subtotal - discounts.map(_.amount).sum // $5.10 - $0.70 = $4.40
  *
  * val receipt = Receipt(subtotal, discounts, total)
  *
  * // Access receipt components
  * receipt.subtotal   // BigDecimal("5.10")
  * receipt.discounts  // List of 2 AppliedDiscount objects
  * receipt.total      // BigDecimal("4.40")
  *
  * // Calculate total savings
  * val totalSavings = receipt.subtotal - receipt.total // BigDecimal("0.70")
  *   }}}
  *
  * @note
  *   The total should always equal subtotal minus the sum of all discount
  *   amounts. This class is immutable and provides a clean summary of the
  *   pricing calculation. The discounts list may be empty if no offers were
  *   applicable to the basket.
  */
final case class Receipt(
    subtotal: BigDecimal,
    discounts: List[AppliedDiscount],
    total: BigDecimal
)
