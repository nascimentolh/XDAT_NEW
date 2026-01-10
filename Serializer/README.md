Serializer
==========
![GitHub](https://img.shields.io/github/license/acmi/serializer)
[![](https://jitpack.io/v/acmi/serializer.svg)](https://jitpack.io/#acmi/serializer)

Simple serialization library.
Based on [L2io](https://github.com/acmi/L2io).

Usage
-----
See [example](src/test/java/acmi/l2/clientmod/io/SerializerTests.java).

Build
-----
```
gradlew build
```
Append `-x test` to skip tests.

Install to local maven repository
---------------------------------
```
gradlew install
```

Maven
-----
```maven
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.acmi</groupId>
    <artifactId>serializer</artifactId>
    <version>1.2.3</version>
</dependency>
```

Gradle
------
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compile group:'com.github.acmi', name:'serializer', version: '1.2.3'
}
```