package com.garybake.shoppingbasket.cli

import com.garybake.shoppingbasket.domain._

@main def Main(args: String*): Unit =
  val args = Seq("Apples", "Apples", "Soup", "Soup", "Bread")
  println("Hello shoppers")
  
  // Create catalog
  val apple = Product(
    sku = "A123",
    name = "Apples",
    unit = "bag",
    unitPrice = 1.00
  )

  val soup = Product(
    sku = "S123",
    name = "Soup",
    unit = "tin",
    unitPrice = 0.65
  )

  val bread = Product(
    sku = "B123",
    name = "Bread",
    unit = "loaf",
    unitPrice = 0.80
  )

  val catalog: Catalog = Catalog.inMemory(Seq(apple, soup, bread))
  val products = ItemParser.default.parse(args, catalog)

  // Place items in basket
  var basket = Basket(Map.empty)
  for (pickedProduct <- args) {
    val product = catalog.getByName(pickedProduct)
    if (product.isDefined) {
      basket = basket.add(product.get)
    }
  }

  println("Basket contents:")
  println(basket.lines.map { case (product, qty) => s"${product.name}: $qty x £${product.unitPrice}" }.mkString("\n"))
  println(s"Subtotal: £${basket.subtotal}")
  
  // Calculate discounts using offer service
  val offerService = OfferService.default
  val discounts = offerService.calculateDiscounts(basket)
  
  if (discounts.nonEmpty) {
    println("\nApplied discounts:")
    discounts.foreach { discount =>
      println(s"${discount.label}: -£${discount.amount}")
    }
    val totalDiscount = discounts.map(_.amount).sum
    println(s"Total savings: £$totalDiscount")
    println(s"Final total: £${basket.subtotal - totalDiscount}")
  } else {
    println("\nNo discounts applicable")
    println(s"Final total: £${basket.subtotal}")
  }

