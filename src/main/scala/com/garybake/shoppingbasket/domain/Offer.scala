package com.garybake.shoppingbasket.domain

/** Contract for any promotional rule. */
sealed trait Offer {
  def label: String
  def active: Boolean

  /** Return zero or more concrete discounts derived from the basket. */
  def applyTo(basket: Basket): List[AppliedDiscount]
}


final case class PercentageOffSingleProduct(
  product: Product,
  percent: BigDecimal,         // e.g., 10 for 10%
  label: String,
  active: Boolean
) extends Offer {
  override def applyTo(basket: Basket): List[AppliedDiscount] = {
    val quantity = basket.quantityOf(product)
    val discount = product.unitPrice * percent / 100 * quantity
    List(AppliedDiscount(label, discount))
  }
}

/** e.g., Buy 2 Soup, get 1 Bread at 50% off. */
final case class BuyNGetMOfOtherAtPercent(
  triggerProduct: Product,
  triggerQty: Int,             // N
  targetProduct: Product,
  targetQtyPerTrigger: Int,    // M (use 1 for the given brief)
  targetPercentOff: BigDecimal, // e.g., 50
  label: String,
  active: Boolean
) extends Offer {
  override def applyTo(basket: Basket): List[AppliedDiscount] = {
    val triggerQty = basket.quantityOf(triggerProduct)
    val targetQty = basket.quantityOf(targetProduct)
    
    // Calculate how many target items can get the discount
    val eligibleTargetQty = (triggerQty / this.triggerQty) * targetQtyPerTrigger
    
    // Don't give more discounts than target items in basket
    val actualDiscountedQty = Math.min(eligibleTargetQty, targetQty)
    
    val discount = targetProduct.unitPrice * targetPercentOff / 100 * actualDiscountedQty
    
    if (discount > 0 && eligibleTargetQty > 0) {
      List(AppliedDiscount(label, discount))
    } else {
      Nil
    }
  }
}