package com.garybake.shoppingbasket.cli

import com.garybake.shoppingbasket.domain._

/** Translates raw CLI tokens to products via catalog. */
trait ItemParser {
  def parse(tokens: Seq[String], catalog: Catalog): Either[String, List[Product]]
}

object ItemParser {
  def default: ItemParser = new ItemParser {
    override def parse(tokens: Seq[String], catalog: Catalog): Either[String, List[Product]] = {
      val products = tokens.map(catalog.getByName).collect { case Some(p) => p }
      Right(products.toList)
    }
  }
}