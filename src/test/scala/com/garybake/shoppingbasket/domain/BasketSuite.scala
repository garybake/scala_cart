package com.garybake.shoppingbasket.domain

import munit.FunSuite
import scala.math.BigDecimal.RoundingMode

class BasketSuite extends FunSuite {

  // Test data
  val apple = Product("APPLE", "Apple", "each", BigDecimal("0.60"))
  val bread = Product("BREAD", "Bread", "loaf", BigDecimal("1.20"))
  val milk = Product("MILK", "Milk", "bottle", BigDecimal("1.30"))

  test("Basket should be empty when created with empty map") {
    val basket = Basket(Map.empty)
    assert(basket.isEmpty)
    assert(basket.lines.isEmpty)
  }

  test("Basket should not be empty when created with products") {
    val basket = Basket(Map(apple -> 2))
    assert(!basket.isEmpty)
    assert(basket.lines.nonEmpty)
  }

  test("add should add new product to empty basket") {
    val basket = Basket(Map.empty)
    val newBasket = basket.add(apple, 3)

    assert(newBasket.quantityOf(apple) == 3)
    assert(newBasket.lines.size == 1)
    assert(newBasket.lines(apple) == 3)
  }

  test("add should add new product to non-empty basket") {
    val basket = Basket(Map(apple -> 2))
    val newBasket = basket.add(bread, 1)

    assert(newBasket.quantityOf(apple) == 2)
    assert(newBasket.quantityOf(bread) == 1)
    assert(newBasket.lines.size == 2)
  }

  test("add should increase quantity of existing product") {
    val basket = Basket(Map(apple -> 2))
    val newBasket = basket.add(apple, 3)

    assert(newBasket.quantityOf(apple) == 5)
    assert(newBasket.lines.size == 1)
  }

  test("add should use default quantity of 1") {
    val basket = Basket(Map.empty)
    val newBasket = basket.add(apple)

    assert(newBasket.quantityOf(apple) == 1)
  }

  test("add should handle zero quantity") {
    val basket = Basket(Map(apple -> 2))
    val newBasket = basket.add(apple, 0)

    assert(newBasket.quantityOf(apple) == 2) // Should not change
  }

  test("add should handle negative quantity") {
    val basket = Basket(Map(apple -> 2))
    val newBasket = basket.add(apple, -1)

    assert(newBasket.quantityOf(apple) == 1) // Should subtract
  }

  test("quantityOf should return 0 for non-existent product") {
    val basket = Basket(Map(apple -> 2))

    assert(basket.quantityOf(bread) == 0)
  }

  test("quantityOf should return correct quantity for existing product") {
    val basket = Basket(Map(apple -> 3, bread -> 1))

    assert(basket.quantityOf(apple) == 3)
    assert(basket.quantityOf(bread) == 1)
  }

  test("lineSubtotal should calculate correct subtotal for product") {
    val basket = Basket(Map(apple -> 3))

    val subtotal = basket.lineSubtotal(apple)
    val expected = BigDecimal("0.60") * 3

    assert(subtotal == expected)
    assert(subtotal == BigDecimal("1.80"))
  }

  test("lineSubtotal should return 0 for non-existent product") {
    val basket = Basket(Map(apple -> 2))

    val subtotal = basket.lineSubtotal(bread)
    assert(subtotal == BigDecimal(0))
  }

  test("lineSubtotal should handle zero quantity") {
    val basket = Basket(Map(apple -> 0))

    val subtotal = basket.lineSubtotal(apple)
    assert(subtotal == BigDecimal(0))
  }

  test("subtotal should calculate total for single product") {
    val basket = Basket(Map(apple -> 2))

    val subtotal = basket.subtotal
    val expected = BigDecimal("0.60") * 2

    assert(subtotal == expected)
    assert(subtotal == BigDecimal("1.20"))
  }

  test("subtotal should calculate total for multiple products") {
    val basket = Basket(
      Map(
        apple -> 2, // 2 * 0.60 = 1.20
        bread -> 1, // 1 * 1.20 = 1.20
        milk -> 3 // 3 * 1.30 = 3.90
      )
    )

    val subtotal = basket.subtotal
    val expected = BigDecimal("1.20") + BigDecimal("1.20") + BigDecimal("3.90")

    assert(subtotal == expected)
    assert(subtotal == BigDecimal("6.30"))
  }

  test("subtotal should return 0 for empty basket") {
    val basket = Basket(Map.empty)

    val subtotal = basket.subtotal
    assert(subtotal == BigDecimal(0))
  }

  test("subtotal should handle decimal precision correctly") {
    val product = Product("TEST", "Test Product", "unit", BigDecimal("0.33"))
    val basket = Basket(Map(product -> 3))

    val subtotal = basket.subtotal
    val expected = BigDecimal("0.99")

    assert(subtotal == expected)
  }

  test("Basket should be immutable - add should not modify original") {
    val originalBasket = Basket(Map(apple -> 1))
    val newBasket = originalBasket.add(apple, 2)

    // Original should remain unchanged
    assert(originalBasket.quantityOf(apple) == 1)
    assert(originalBasket.lines.size == 1)

    // New basket should have updated values
    assert(newBasket.quantityOf(apple) == 3)
    assert(newBasket.lines.size == 1)
  }

  test("Basket should handle multiple add operations") {
    val basket = Basket(Map.empty)
      .add(apple, 2)
      .add(bread, 1)
      .add(apple, 1)
      .add(milk, 2)

    assert(basket.quantityOf(apple) == 3)
    assert(basket.quantityOf(bread) == 1)
    assert(basket.quantityOf(milk) == 2)
    assert(basket.lines.size == 3)
  }
}
