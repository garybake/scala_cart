package com.garybake.shoppingbasket.cli

import com.garybake.shoppingbasket.domain._

def createCatalog(): Catalog =
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

  val milk = Product(
    sku = "M123",
    name = "Milk",
    unit = "bottle",
    unitPrice = 1.30
  )

  Catalog.inMemory(Seq(apple, soup, bread))

@main def Main(args: String*): Unit =

  val catalog: Catalog = createCatalog()  
  val products = ItemParser.default.parse(args, catalog)

  // Place items in basket
  var basket = Basket(Map.empty)
  for (pickedProduct <- args) {
    val product = catalog.getByName(pickedProduct)
    if (product.isDefined) {
      basket = basket.add(product.get)
    }
  }

  println(s"Subtotal: £${basket.subtotal}")
  
  // Calculate discounts using offer service
  val offerService = OfferService.default
  val discounts = offerService.calculateDiscounts(basket)
  
  if (discounts.nonEmpty) {
    discounts.foreach { discount =>
      println(s"${discount.label}: £${discount.amount}")
    }
    val totalDiscount = discounts.map(_.amount).sum
    println(s"Total price: £${basket.subtotal - totalDiscount}")
  } else {
    println("(No offers available)")
    println(s"Total price: £${basket.subtotal}")
  }

