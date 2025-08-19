package com.garybake.shoppingbasket.domain

/** Represents a shopping basket containing products and their quantities.
  *
  * A basket maintains a mapping of products to their quantities and provides
  * methods for adding products, calculating subtotals, and checking basket
  * state.
  *
  * @param lines
  *   A map where keys are products and values are their quantities
  *
  * @example
  *   {{{
  * val apple = Product("apple", BigDecimal("0.60"))
  * val basket = Basket(Map.empty)
  * val basketWithApple = basket.add(apple, 2)
  * val subtotal = basketWithApple.subtotal // BigDecimal("1.20")
  *   }}}
  */
final case class Basket(lines: Map[Product, Int]) {

  /** Adds a product to the basket with the specified quantity.
    *
    * If the product already exists in the basket, the quantity is added to the
    * existing quantity. If it's a new product, it's added with the specified
    * quantity.
    *
    * @param product
    *   The product to add to the basket
    * @param qty
    *   The quantity to add (defaults to 1)
    * @return
    *   A new Basket instance with the updated product quantities
    *
    * @example
    *   {{{
    * val basket = Basket(Map.empty)
    * val updatedBasket = basket.add(Product("apple", BigDecimal("0.60")), 3)
    * updatedBasket.quantityOf(Product("apple", BigDecimal("0.60"))) // 3
    *   }}}
    */
  def add(product: Product, qty: Int = 1): Basket = {
    val newLines = lines.updated(product, lines.getOrElse(product, 0) + qty)
    Basket(newLines)
  }

  /** Gets the quantity of a specific product in the basket.
    *
    * @param product
    *   The product to check the quantity for
    * @return
    *   The quantity of the product, or 0 if the product is not in the basket
    *
    * @example
    *   {{{
    * val basket = Basket(Map(Product("apple", BigDecimal("0.60")) -> 2))
    * basket.quantityOf(Product("apple", BigDecimal("0.60"))) // 2
    * basket.quantityOf(Product("bread", BigDecimal("1.00"))) // 0
    *   }}}
    */
  def quantityOf(product: Product): Int = lines.getOrElse(product, 0)

  /** Calculates the subtotal for a specific product line.
    *
    * @param product
    *   The product to calculate the line subtotal for
    * @return
    *   The subtotal for this product line (unit price × quantity)
    *
    * @example
    *   {{{
    * val apple = Product("apple", BigDecimal("0.60"))
    * val basket = Basket(Map(apple -> 3))
    * basket.lineSubtotal(apple) // BigDecimal("1.80")
    *   }}}
    */
  def lineSubtotal(product: Product): BigDecimal =
    product.unitPrice * BigDecimal(quantityOf(product))

  /** Calculates the total subtotal for all products in the basket.
    *
    * @return
    *   The sum of all line subtotals
    *
    * @example
    *   {{{
    * val apple = Product("apple", BigDecimal("0.60"))
    * val bread = Product("bread", BigDecimal("1.00"))
    * val basket = Basket(Map(apple -> 2, bread -> 1))
    * basket.subtotal // BigDecimal("2.20")
    *   }}}
    */
  def subtotal: BigDecimal = lines.map { case (product, qty) =>
    lineSubtotal(product)
  }.sum

  /** Checks if the basket contains any products.
    *
    * @return
    *   true if the basket is empty (no products), false otherwise
    *
    * @example
    *   {{{
    * val emptyBasket = Basket(Map.empty)
    * emptyBasket.isEmpty // true
    *
    * val basketWithItems = Basket(Map(Product("apple", BigDecimal("0.60")) -> 1))
    * basketWithItems.isEmpty // false
    *   }}}
    */
  def isEmpty: Boolean = lines.isEmpty
}
