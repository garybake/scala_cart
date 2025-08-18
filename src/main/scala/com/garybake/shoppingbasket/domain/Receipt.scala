package com.garybake.shoppingbasket.domain


/** Final pricing result. */
final case class Receipt(
  subtotal: BigDecimal,
  discounts: List[AppliedDiscount],
  total: BigDecimal
)