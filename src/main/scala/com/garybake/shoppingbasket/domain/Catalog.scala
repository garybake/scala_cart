package com.garybake.shoppingbasket.domain

trait Catalog {
  def getBySku(sku: String): Option[Product]
  def getByName(name: String): Option[Product]
  def all: Vector[Product]
}

object Catalog {
  /** Simple in-memory catalog factory. */
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