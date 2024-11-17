[![Build Status](https://travis-ci.org/dwhjames/datomisca.svg?branch=master)](https://travis-ci.org/dwhjames/datomisca) [![Download](https://api.bintray.com/packages/dwhjames/maven/datomisca/images/download.svg) ](https://bintray.com/dwhjames/maven/datomisca/_latestVersion)

# [Datomisca](https://dwhjames.github.io/datomisca), embrace Datomic the Scala way

### _A Scala API for [Datomic](http://www.datomic.com)_

Please go to [Datomisca website](https://dwhjames.github.io/datomisca) for full description of the project and its [features](https://dwhjames.github.io/datomisca/doc/features.html), as well as a guide for [getting started](https://dwhjames.github.io/datomisca/doc/getstarted.html) and samples!

## <a name="philosophy">The philosophy of Datomisca in a nutshell</a>

### <a name="philosophy-embrace">Datomic principles, without compromise</a>
Datomisca is a thin layer around Datomic aimed at exposing Datomic’s functionality and leveraging its full power.

### <a name="philosophy-enhance">Datomic features with a Scala flavor</a>

Datomisca uses Scala concepts to enhance the Datomic experience for Scala developers:

- Type safety, 
- Asynchronicity & non-blocking patterns, 
- Advanced functional programming
- Compile-time enhancement with Scala 2.10 macros

## Versions
* **TRUNK** [not released in the repository, yet]
    * Fancy contributing something? :-)
* **0.8.5** [release on 2024-11-17]
  * scala 2.13.15, back support 2.12
* **0.8.4** [release on 2024-11-10]
  * scala 2.13.15
* **0.8.3** [release on 2024-11-10]
  * scala 2.13.11
* **0.7.3** [release on 2024-11-09]
  * Playframework 2.8.0
* **0.7.2** [release on 2023-09-13]
  * Playframework 2.8.0, scala 2.13.11   
* **0.7.1** [release on 2019-12-24]
    * Playframework 2.8.0, scala 2.12/2.13  
* **0.7.0** [release on 2019-12-8]
    * scala 2.12

## Test
sbt project integrationTests
it:test

## Release
sbt +publishSigned
sbt sonatypeRelease
