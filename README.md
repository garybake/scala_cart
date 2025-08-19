## Scala Cart

[![CI](https://github.com/garybake/scala_cart/actions/workflows/ci.yml/badge.svg)](https://github.com/garybake/scala_cart/actions/workflows/ci.yml)

Technical assignment to build a shopping basket in scala.

### Domain

- Product - Item in the shop for sale identified by sku.
- Catalog - Set of distinct products available at the store.
- Basket - A list of products intended to be purchased.
- Offer - A discount on products i.e. 10% off apples.
- Receipt - Combination of Basket and offers to provide a final price.

### Requirements

Ensure you have scala 3 and sbt installed.

### Usage

    sbt "run Apples Apples Soup" 
 
Add in as many products as needed.
Current products are Apples, Milk, Bread and Soup


### Development

Project was created using

    sbt new scala/scala3.g8

Test 

    sbt test