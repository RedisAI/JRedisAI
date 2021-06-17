[![license](https://img.shields.io/github/license/RedisAI/JRedisAI.svg)](https://github.com/RedisAI/JRedisAI)
[![CircleCI](https://circleci.com/gh/RedisAI/JRedisAI/tree/master.svg?style=svg)](https://circleci.com/gh/RedisAI/JRedisAI/tree/master)
[![GitHub issues](https://img.shields.io/github/release/RedisAI/JRedisAI.svg)](https://github.com/RedisAI/JRedisAI/releases/latest)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.redislabs/jredisai/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.redislabs/jredisai)
[![Javadocs](https://www.javadoc.io/badge/com.redislabs/jredisai.svg)](https://www.javadoc.io/doc/com.redislabs/jredisai)
[![codecov](https://codecov.io/gh/RedisAI/JRedisAI/branch/master/graph/badge.svg?token=cC4H2TvQHs)](https://codecov.io/gh/RedisAI/JRedisAI)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/RedisAI/JRedisAI.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/RedisAI/JRedisAI/context:java)

# JRedisAI
[![Forum](https://img.shields.io/badge/Forum-RedisAI-blue)](https://forum.redislabs.com/c/modules/redisai)
[![Discord](https://img.shields.io/discord/697882427875393627?style=flat-square)](https://discord.gg/rTQm7UZ)

Java client for RedisAI

### Official Releases

```xml
  <dependencies>
    <dependency>
      <groupId>com.redislabs</groupId>
      <artifactId>jredisai</artifactId>
      <version>0.9.0</version>
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
      <version>1.0.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
```

# Example: Using the Java Client

```java
   RedisAI client = new RedisAI("localhost", 6379);
   client.setModel("model", Backend.TF, Device.CPU, new String[] {"a", "b"}, new String[] {"mul"}, "graph.pb");
    
   client.setTensor("a", new float[] {2, 3}, new int[]{2});
   client.setTensor("b", new float[] {2, 3}, new int[]{2});

   client.runModel("model", new String[] {"a", "b"}, new String[] {"c"});
```
