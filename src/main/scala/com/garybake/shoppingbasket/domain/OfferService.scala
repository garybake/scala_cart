package com.garybake.shoppingbasket.domain

/** Service for managing offers and calculating applicable discounts. */
trait OfferService {
  def availableOffers: List[Offer]
  def calculateDiscounts(basket: Basket): List[AppliedDiscount]
}

object OfferService {
  /** Simple in-memory offer service. */
  def inMemory(offers: List[Offer]): OfferService = new OfferService {
    override def availableOffers: List[Offer] = offers
    
    override def calculateDiscounts(basket: Basket): List[AppliedDiscount] = {
      if (basket.isEmpty) return Nil
      
      // Apply all active offers to the basket
      availableOffers
        .filter(_.active)
        .flatMap(_.applyTo(basket))
        .filter(_.amount > 0)
    }
  }
  
  /** Create a default offer service with common promotional rules. */
  def default: OfferService = {
    val apple = Product("A123", "Apples", "bag", 1.00)
    val soup = Product("S123", "Soup", "tin", 0.65)
    val bread = Product("B123", "Bread", "loaf", 0.80)
    
    val offers = List(
      // 10% off apples
      PercentageOffSingleProduct(
        product = apple,
        percent = 10,
        label = "Apples 10% off",
        active = true
      ),
      // Buy 2 soup, get 1 bread at 50% off
      BuyNGetMOfOtherAtPercent(
        triggerProduct = soup,
        triggerQty = 2,
        targetProduct = bread,
        targetQtyPerTrigger = 1,
        targetPercentOff = 50,
        label = "Buy 2 Soup, get Bread 50% off",
        active = true
      )
    )
    
    inMemory(offers)
  }
}

