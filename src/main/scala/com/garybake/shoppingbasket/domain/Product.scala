package com.garybake.shoppingbasket.domain

final case class Product(
  sku: String,
  name: String,
  unit: String,  // tin, loaf, bottle, bag, etc.
  unitPrice: BigDecimal
) {
  override def toString: String =
    s"$name ($sku): £$unitPrice per $unit"
}