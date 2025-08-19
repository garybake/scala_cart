package com.garybake.shoppingbasket.domain

/** Represents a single applied discount line on a receipt.
  *
  * This case class captures information about a discount that has been applied
  * to a shopping basket, including a human-readable description of the discount
  * and the monetary amount that was deducted from the total.
  *
  * @param label
  *   A human-readable description of the discount (e.g., "Apples 10% off")
  * @param amount
  *   The positive amount deducted from the total (in the same currency as
  *   products)
  *
  * @example
  *   {{{
  * // Create a discount for 10% off apples
  * val appleDiscount = AppliedDiscount("Apples 10% off", BigDecimal("0.12"))
  *
  * // Create a buy-one-get-one-free discount
  * val bogoDiscount = AppliedDiscount("BOGO Bread", BigDecimal("1.00"))
  *
  * // The amount should always be positive (what was deducted)
  * val totalDiscount = appleDiscount.amount + bogoDiscount.amount // BigDecimal("1.12")
  *   }}}
  *
  * @note
  *   The amount field represents the positive value that was deducted from the
  *   basket total. For example, if a product costs $1.00 and gets a 50%
  *   discount, the amount would be $0.50 (not -$0.50).
  */
final case class AppliedDiscount(
    label: String, // e.g., "Apples 10% off"
    amount: BigDecimal // positive amount deducted
)
