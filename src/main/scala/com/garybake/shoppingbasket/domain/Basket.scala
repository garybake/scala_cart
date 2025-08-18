package com.garybake.shoppingbasket.domain

final case class Basket(lines: Map[Product, Int]) {
  def add(product: Product, qty: Int = 1): Basket = {
    val newLines = lines.updated(product, lines.getOrElse(product, 0) + qty)
    Basket(newLines)
  }
  def quantityOf(product: Product): Int = lines.getOrElse(product, 0)
  def lineSubtotal(product: Product): BigDecimal = product.unitPrice * BigDecimal(quantityOf(product))
  def subtotal: BigDecimal = lines.map { case (product, qty) => lineSubtotal(product) }.sum
  def isEmpty: Boolean = lines.isEmpty
}