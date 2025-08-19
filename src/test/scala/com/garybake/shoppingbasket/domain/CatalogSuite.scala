package com.garybake.shoppingbasket.domain

import munit.FunSuite

class CatalogSuite extends FunSuite {
  test("Catalog.inMemory creates catalog with correct products") {
    val apple = Product("A123", "Apples", "bag", BigDecimal(1.00))
    val soup = Product("S123", "Soup", "tin", BigDecimal(0.65))
    val bread = Product("B123", "Bread", "loaf", BigDecimal(0.80))

    val catalog = Catalog.inMemory(List(apple, soup, bread))

    // Test getBySku
    assertEquals(catalog.getBySku("A123"), Some(apple))
    assertEquals(catalog.getBySku("S123"), Some(soup))
    assertEquals(catalog.getBySku("B123"), Some(bread))
    assertEquals(catalog.getBySku("NONEXISTENT"), None)

    // Test getByName (case insensitive)
    assertEquals(catalog.getByName("Apples"), Some(apple))
    assertEquals(catalog.getByName("apples"), Some(apple))
    assertEquals(catalog.getByName("APPLES"), Some(apple))
    assertEquals(catalog.getByName("Soup"), Some(soup))
    assertEquals(catalog.getByName("soup"), Some(soup))
    assertEquals(catalog.getByName("Bread"), Some(bread))
    assertEquals(catalog.getByName("bread"), Some(bread))
    assertEquals(catalog.getByName("Nonexistent"), None)

    // Test all products
    val allProducts = catalog.all
    assertEquals(allProducts.length, 3)
    assert(allProducts.contains(apple))
    assert(allProducts.contains(soup))
    assert(allProducts.contains(bread))
  }

  test("Catalog.inMemory handles empty product list") {
    val catalog = Catalog.inMemory(List.empty)

    assertEquals(catalog.getBySku("ANY"), None)
    assertEquals(catalog.getByName("Any"), None)
    assertEquals(catalog.all, Vector.empty)
  }

  test("Catalog.inMemory handles single product") {
    val singleProduct =
      Product("SINGLE", "Single Product", "unit", BigDecimal(5.99))
    val catalog = Catalog.inMemory(List(singleProduct))

    assertEquals(catalog.getBySku("SINGLE"), Some(singleProduct))
    assertEquals(catalog.getByName("Single Product"), Some(singleProduct))
    assertEquals(catalog.getByName("single product"), Some(singleProduct))
    assertEquals(catalog.all, Vector(singleProduct))
  }

  test("Catalog.inMemory handles duplicate SKUs (last one wins)") {
    val product1 = Product("DUPE", "First Product", "unit", BigDecimal(1.00))
    val product2 = Product("DUPE", "Second Product", "unit", BigDecimal(2.00))

    val catalog = Catalog.inMemory(List(product1, product2))

    // Last product with duplicate SKU should win
    assertEquals(catalog.getBySku("DUPE"), Some(product2))
    assertEquals(catalog.getByName("First Product"), Some(product1))
    assertEquals(catalog.getByName("Second Product"), Some(product2))
    assertEquals(catalog.all.length, 2)
  }

  test("Catalog.inMemory handles duplicate names (case insensitive)") {
    val product1 = Product("SKU1", "Duplicate Name", "unit", BigDecimal(1.00))
    val product2 = Product("SKU2", "duplicate name", "unit", BigDecimal(2.00))

    val catalog = Catalog.inMemory(List(product1, product2))

    // Last product with duplicate name should win
    assertEquals(catalog.getByName("Duplicate Name"), Some(product2))
    assertEquals(catalog.getByName("duplicate name"), Some(product2))
    assertEquals(catalog.getByName("DUPLICATE NAME"), Some(product2))
    assertEquals(catalog.getBySku("SKU1"), Some(product1))
    assertEquals(catalog.getBySku("SKU2"), Some(product2))
    assertEquals(catalog.all.length, 2)
  }

  test("Catalog.inMemory handles special characters in names and SKUs") {
    val product1 =
      Product("SKU-123", "Product & Name", "unit", BigDecimal(1.00))
    val product2 = Product("SKU_456", "Product-Name", "unit", BigDecimal(2.00))
    val product3 = Product("SKU.789", "Product.Name", "unit", BigDecimal(3.00))

    val catalog = Catalog.inMemory(List(product1, product2, product3))

    assertEquals(catalog.getBySku("SKU-123"), Some(product1))
    assertEquals(catalog.getBySku("SKU_456"), Some(product2))
    assertEquals(catalog.getBySku("SKU.789"), Some(product3))

    assertEquals(catalog.getByName("Product & Name"), Some(product1))
    assertEquals(catalog.getByName("product & name"), Some(product1))
    assertEquals(catalog.getByName("Product-Name"), Some(product2))
    assertEquals(catalog.getByName("Product.Name"), Some(product3))
  }

  test("Catalog.inMemory handles products with same name but different cases") {
    val product1 = Product("SKU1", "Product", "unit", BigDecimal(1.00))
    val product2 = Product("SKU2", "PRODUCT", "unit", BigDecimal(2.00))
    val product3 = Product("SKU3", "product", "unit", BigDecimal(3.00))

    val catalog = Catalog.inMemory(List(product1, product2, product3))

    // Since names are stored in lowercase, the last product with the same name (case-insensitive) wins
    // This is the current behavior of the implementation
    assertEquals(
      catalog.getByName("Product"),
      Some(product3)
    ) // product3 wins (last one)
    assertEquals(
      catalog.getByName("PRODUCT"),
      Some(product3)
    ) // product3 wins (last one)
    assertEquals(
      catalog.getByName("product"),
      Some(product3)
    ) // product3 wins (last one)

    // All SKUs should still be findable
    assertEquals(catalog.getBySku("SKU1"), Some(product1))
    assertEquals(catalog.getBySku("SKU2"), Some(product2))
    assertEquals(catalog.getBySku("SKU3"), Some(product3))

    // All products should be in the catalog
    assertEquals(catalog.all.length, 3)
    assert(catalog.all.contains(product1))
    assert(catalog.all.contains(product2))
    assert(catalog.all.contains(product3))
  }
}
