package com.garybake.shoppingbasket.domain

/** A catalog that provides access to products by various identifiers.
  *
  * The catalog allows products to be retrieved by SKU (Stock Keeping Unit), by
  * name (case-insensitive), or to get all available products.
  *
  * @example
  *   {{{
  * val products = Seq(
  *   Product("APPLE", "apple", BigDecimal("0.60")),
  *   Product("BREAD", "bread", BigDecimal("1.00"))
  * )
  * val catalog = Catalog.inMemory(products)
  * val apple = catalog.getByName("apple") // Some(Product("APPLE", "apple", 0.60))
  * val bread = catalog.getBySku("BREAD") // Some(Product("BREAD", "bread", 1.00))
  *   }}}
  */
trait Catalog {

  /** Retrieves a product by its SKU (Stock Keeping Unit).
    *
    * @param sku
    *   The SKU identifier for the product
    * @return
    *   Some(Product) if found, None if no product exists with that SKU
    *
    * @example
    *   {{{
    * val catalog = Catalog.inMemory(Seq(Product("APPLE", "apple", BigDecimal("0.60"))))
    * catalog.getBySku("APPLE") // Some(Product("APPLE", "apple", 0.60))
    * catalog.getBySku("NONEXISTENT") // None
    *   }}}
    */
  def getBySku(sku: String): Option[Product]

  /** Retrieves a product by its name (case-insensitive).
    *
    * @param name
    *   The name of the product (will be converted to lowercase for lookup)
    * @return
    *   Some(Product) if found, None if no product exists with that name
    *
    * @example
    *   {{{
    * val catalog = Catalog.inMemory(Seq(Product("APPLE", "apple", BigDecimal("0.60"))))
    * catalog.getByName("apple") // Some(Product("APPLE", "apple", 0.60))
    * catalog.getByName("APPLE") // Some(Product("APPLE", "apple", 0.60))
    * catalog.getByName("Apple") // Some(Product("APPLE", "apple", 0.60))
    * catalog.getByName("nonexistent") // None
    *   }}}
    */
  def getByName(name: String): Option[Product]

  /** Retrieves all products in the catalog.
    *
    * @return
    *   A Vector containing all available products
    *
    * @example
    *   {{{
    * val products = Seq(
    *   Product("APPLE", "apple", BigDecimal("0.60")),
    *   Product("BREAD", "bread", BigDecimal("1.00"))
    * )
    * val catalog = Catalog.inMemory(products)
    * catalog.all // Vector(Product("APPLE", "apple", 0.60), Product("BREAD", "bread", 1.00))
    *   }}}
    */
  def all: Vector[Product]
}

object Catalog {

  /** Creates a simple in-memory catalog implementation.
    *
    * This factory method creates a catalog that stores products in memory using
    * Maps for efficient lookups by SKU and name. The name lookup is
    * case-insensitive for better user experience.
    *
    * @param products
    *   The sequence of products to include in the catalog
    * @return
    *   A new Catalog instance with the specified products
    *
    * @example
    *   {{{
    * val products = Seq(
    *   Product("APPLE", "apple", BigDecimal("0.60")),
    *   Product("BREAD", "bread", BigDecimal("1.00")),
    *   Product("MILK", "milk", BigDecimal("1.30"))
    * )
    * val catalog = Catalog.inMemory(products)
    *
    * // Lookup by SKU
    * val apple = catalog.getBySku("APPLE")
    *
    * // Lookup by name (case-insensitive)
    * val bread = catalog.getByName("BREAD")
    * val milk = catalog.getByName("milk")
    *
    * // Get all products
    * val allProducts = catalog.all
    *   }}}
    */
  def inMemory(products: Seq[Product]): Catalog = new Catalog {
    private val bySku: Map[String, Product] =
      products.map(p => p.sku -> p).toMap

    private val byName: Map[String, Product] =
      products.map(p => p.name.toLowerCase -> p).toMap

    override def getBySku(sku: String): Option[Product] =
      bySku.get(sku)

    override def getByName(name: String): Option[Product] =
      byName.get(name.toLowerCase)

    override def all: Vector[Product] =
      products.toVector
  }
}
