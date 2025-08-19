package com.garybake.shoppingbasket.cli

import com.garybake.shoppingbasket.domain._

/** 
 * Translates raw CLI tokens to products via catalog.
 * 
 * This trait provides functionality to parse command-line input tokens
 * and convert them into a list of products by looking them up in a catalog.
 * 
 * @example
 * {{{
 * val parser = ItemParser.default
 * val catalog = Catalog.default
 * val tokens = Seq("apple", "bread", "milk")
 * val result = parser.parse(tokens, catalog)
 * // result: Either[String, List[Product]]
 * }}}
 */
trait ItemParser {
  /**
   * Parses a sequence of string tokens and converts them to products.
   * 
   * @param tokens The raw string tokens from command-line input
   * @param catalog The catalog to look up products by name
   * @return Either a list of successfully found products, or an error message string
   *         if parsing fails
   */
  def parse(
      tokens: Seq[String],
      catalog: Catalog
  ): Either[String, List[Product]]
}

object ItemParser {
  /**
   * Provides the default implementation of ItemParser.
   * 
   * This implementation:
   * - Maps each token to a product lookup in the catalog
   * - Collects only successfully found products (filters out None results)
   * - Returns a Right with the list of products if any are found
   * - Never returns an error (Left) - missing products are simply filtered out
   * 
   * @return A default ItemParser implementation
   * 
   * @example
   * {{{
   * val parser = ItemParser.default
   * val result = parser.parse(Seq("apple", "nonexistent", "bread"), catalog)
   * // If "apple" and "bread" exist in catalog but "nonexistent" doesn't:
   * // result = Right(List(appleProduct, breadProduct))
   * }}}
   */
  def default: ItemParser = new ItemParser {
    override def parse(
        tokens: Seq[String],
        catalog: Catalog
    ): Either[String, List[Product]] = {
      val products = tokens.map(catalog.getByName).collect { case Some(p) => p }
      Right(products.toList)
    }
  }
}
