package com.garybake.shoppingbasket.domain

/** Represents a product that can be added to a shopping basket.
  *
  * A Product defines the essential attributes of an item that customers can
  * purchase, including its unique identifier (SKU), display name, unit of
  * measurement, and price. Products are immutable and serve as the foundation
  * for basket operations and discount calculations.
  *
  * @param sku
  *   The Stock Keeping Unit - a unique identifier for the product
  * @param name
  *   The human-readable name of the product for display purposes
  * @param unit
  *   The unit of measurement (e.g., "tin", "loaf", "bottle", "bag")
  * @param unitPrice
  *   The price per unit in the system's currency
  *
  * @example
  *   {{{
  * // Create basic products
  * val apple = Product("A123", "Apples", "bag", BigDecimal("1.00"))
  * val soup = Product("S456", "Soup", "tin", BigDecimal("0.65"))
  * val bread = Product("B789", "Bread", "loaf", BigDecimal("0.80"))
  * val milk = Product("M101", "Milk", "bottle", BigDecimal("1.30"))
  *
  * // Access product properties
  * apple.sku        // "A123"
  * apple.name       // "Apples"
  * apple.unit       // "bag"
  * apple.unitPrice  // BigDecimal("1.00")
  *
  * // Use in basket operations
  * val basket = Basket(Map.empty)
  * val basketWithApples = basket.add(apple, 3)
  * val appleSubtotal = basketWithApples.lineSubtotal(apple) // BigDecimal("3.00")
  *   }}}
  *
  * @note
  *   Products are case classes, so they provide automatic equality comparison,
  *   pattern matching, and copying capabilities. The SKU should be unique
  *   within a catalog to avoid conflicts.
  */
final case class Product(
    sku: String,
    name: String,
    unit: String, // tin, loaf, bottle, bag, etc.
    unitPrice: BigDecimal
)
