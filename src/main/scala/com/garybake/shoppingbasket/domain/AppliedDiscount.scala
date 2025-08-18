package com.garybake.shoppingbasket.domain

/** A single applied discount line on the receipt. */
final case class AppliedDiscount(
  label: String,          // e.g., "Apples 10% off"
  amount: BigDecimal,          // positive amount deducted
)