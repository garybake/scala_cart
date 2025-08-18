package com.garybake.shoppingbasket.cli

import munit.FunSuite
import com.garybake.shoppingbasket.domain._

class ItemParserSuite extends FunSuite {
  
  // Test data
  val apple = Product("A123", "Apples", "bag", BigDecimal("1.00"))
  val soup = Product("S123", "Soup", "tin", BigDecimal("0.65"))
  val bread = Product("B123", "Bread", "loaf", BigDecimal("0.80"))
  val milk = Product("M456", "Milk", "bottle", BigDecimal("1.30"))
  
  val catalog = Catalog.inMemory(List(apple, soup, bread, milk))
  
  test("ItemParser.default should parse valid product names") {
    val parser = ItemParser.default
    val tokens = Seq("Apples", "Soup", "Bread")
    
    val result = parser.parse(tokens, catalog)
    
    assert(result.isRight)
    result.foreach { products =>
      assert(products.length == 3)
      assert(products.contains(apple))
      assert(products.contains(soup))
      assert(products.contains(bread))
    }
  }
  
  test("ItemParser.default should handle single product") {
    val parser = ItemParser.default
    val tokens = Seq("Milk")
    
    val result = parser.parse(tokens, catalog)
    
    assert(result.isRight)
    result.foreach { products =>
      assert(products.length == 1)
      assert(products.head == milk)
    }
  }
  
  test("ItemParser.default should handle empty tokens") {
    val parser = ItemParser.default
    val tokens = Seq.empty[String]
    
    val result = parser.parse(tokens, catalog)
    
    assert(result.isRight)
    result.foreach { products =>
      assert(products.isEmpty)
    }
  }
  
  test("ItemParser.default should handle case-insensitive product names") {
    val parser = ItemParser.default
    val tokens = Seq("apples", "SOUP", "Bread", "mIlK")
    
    val result = parser.parse(tokens, catalog)
    
    assert(result.isRight)
    result.foreach { products =>
      assert(products.length == 4)
      assert(products.contains(apple))
      assert(products.contains(soup))
      assert(products.contains(bread))
      assert(products.contains(milk))
    }
  }
  
  test("ItemParser.default should filter out invalid product names") {
    val parser = ItemParser.default
    val tokens = Seq("Apples", "InvalidProduct", "Soup", "NonExistent", "Bread")
    
    val result = parser.parse(tokens, catalog)
    
    assert(result.isRight)
    result.foreach { products =>
      assert(products.length == 3)
      assert(products.contains(apple))
      assert(products.contains(soup))
      assert(products.contains(bread))
      assert(!products.exists(_.name == "InvalidProduct"))
      assert(!products.exists(_.name == "NonExistent"))
    }
  }
  
  test("ItemParser.default should handle all invalid product names") {
    val parser = ItemParser.default
    val tokens = Seq("InvalidProduct1", "NonExistent", "UnknownItem")
    
    val result = parser.parse(tokens, catalog)
    
    assert(result.isRight)
    result.foreach { products =>
      assert(products.isEmpty)
    }
  }
  
  test("ItemParser.default should handle duplicate product names") {
    val parser = ItemParser.default
    val tokens = Seq("Apples", "Soup", "Apples", "Bread", "Apples")
    
    val result = parser.parse(tokens, catalog)
    
    assert(result.isRight)
    result.foreach { products =>
      assert(products.length == 5)
      assert(products.count(_ == apple) == 3)
      assert(products.count(_ == soup) == 1)
      assert(products.count(_ == bread) == 1)
    }
  }
  
  test("ItemParser.default should handle mixed valid and invalid tokens") {
    val parser = ItemParser.default
    val tokens = Seq("Invalid", "Apples", "Unknown", "Soup", "NonExistent", "Bread")
    
    val result = parser.parse(tokens, catalog)
    
    assert(result.isRight)
    result.foreach { products =>
      assert(products.length == 3)
      assert(products.contains(apple))
      assert(products.contains(soup))
      assert(products.contains(bread))
    }
  }
  
  test("ItemParser.default should handle whitespace in product names") {
    val parser = ItemParser.default
    val tokens = Seq("  Apples  ", "  Soup  ", "  Bread  ")
    
    val result = parser.parse(tokens, catalog)
    
    assert(result.isRight)
    result.foreach { products =>
      // The parser doesn't trim whitespace, so these won't be found
      // and will be filtered out, resulting in an empty list
      assert(products.isEmpty)
    }
  }
  
  test("ItemParser.default should work with different catalog instances") {
    val parser = ItemParser.default
    val customCatalog = Catalog.inMemory(List(
      Product("C123", "Cheese", "block", BigDecimal("2.50")),
      Product("W123", "Wine", "bottle", BigDecimal("8.00"))
    ))
    
    val tokens = Seq("Cheese", "Wine")
    val result = parser.parse(tokens, customCatalog)
    
    assert(result.isRight)
    result.foreach { products =>
      assert(products.length == 2)
      assert(products.exists(_.name == "Cheese"))
      assert(products.exists(_.name == "Wine"))
    }
  }
  
  test("ItemParser.default should handle empty catalog") {
    val parser = ItemParser.default
    val emptyCatalog = Catalog.inMemory(List.empty)
    val tokens = Seq("Apples", "Soup")
    
    val result = parser.parse(tokens, emptyCatalog)
    
    assert(result.isRight)
    result.foreach { products =>
      assert(products.isEmpty)
    }
  }
  
  test("ItemParser.default should preserve product order from tokens") {
    val parser = ItemParser.default
    val tokens = Seq("Milk", "Apples", "Bread", "Soup")
    
    val result = parser.parse(tokens, catalog)
    
    assert(result.isRight)
    result.foreach { products =>
      assert(products.length == 4)
      assert(products(0) == milk)
      assert(products(1) == apple)
      assert(products(2) == bread)
      assert(products(3) == soup)
    }
  }
  
  test("ItemParser.default should handle special characters in product names") {
    val parser = ItemParser.default
    val specialCatalog = Catalog.inMemory(List(
      Product("SP1", "Product-Name", "unit", BigDecimal("1.00")),
      Product("SP2", "Product_Name", "unit", BigDecimal("2.00")),
      Product("SP3", "Product.Name", "unit", BigDecimal("3.00"))
    ))
    
    val tokens = Seq("Product-Name", "Product_Name", "Product.Name")
    val result = parser.parse(tokens, specialCatalog)
    
    assert(result.isRight)
    result.foreach { products =>
      assert(products.length == 3)
      assert(products.exists(_.name == "Product-Name"))
      assert(products.exists(_.name == "Product_Name"))
      assert(products.exists(_.name == "Product.Name"))
    }
  }
}

