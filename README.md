[![license](https://img.shields.io/github/license/RedisAI/JRedisAI.svg)](https://github.com/RedisAI/JRedisAI)
[![CircleCI](https://circleci.com/gh/RedisAI/JRedisAI/tree/master.svg?style=svg)](https://circleci.com/gh/RedisAI/JRedisAI/tree/master)
[![GitHub issues](https://img.shields.io/github/release/RedisAI/JRedisAI.svg)](https://github.com/RedisAI/JRedisAI/releases/latest)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.redislabs/jredisai/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.redislabs/jredisai)
[![Javadocs](https://www.javadoc.io/badge/com.redislabs/jredisai.svg)](https://www.javadoc.io/doc/com.redislabs/jredisai)
[![Codecov](https://codecov.io/gh/RedisAI/JRedisAI/branch/master/graph/badge.svg)](https://codecov.io/gh/RedisAI/JRedisAI)

# JRedisAI
Java client for RedisAI

### Official Releases

```xml
  <dependencies>
    <dependency>
      <groupId>com.redislabs</groupId>
      <artifactId>jredisai</artifactId>
      <version>0.3.0</version>
    </dependency>
  </dependencies>
```

### Snapshots

```xml
  <repositories>
    <repository>
      <id>snapshots-repo</id>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    </repository>
  </repositories>
```

and

```xml
  <dependencies>
    <dependency>
      <groupId>com.redislabs</groupId>
      <artifactId>jRedisAI</artifactId>
      <version>0.4.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
```

# Example: Using the Java Client

```java
   Pool<Jedis> pool = ...
   RedisAI rts = new RedisAI(pool);
```
