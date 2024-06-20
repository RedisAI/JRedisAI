[![license](https://img.shields.io/github/license/RedisAI/JRedisAI.svg)](https://github.com/RedisAI/JRedisAI)
[![CircleCI](https://circleci.com/gh/RedisAI/JRedisAI/tree/master.svg?style=svg)](https://circleci.com/gh/RedisAI/JRedisAI/tree/master)
[![GitHub issues](https://img.shields.io/github/release/RedisAI/JRedisAI.svg)](https://github.com/RedisAI/JRedisAI/releases/latest)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.redislabs/jredisai/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.redislabs/jredisai)
[![Javadocs](https://www.javadoc.io/badge/com.redislabs/jredisai.svg)](https://www.javadoc.io/doc/com.redislabs/jredisai)
[![codecov](https://codecov.io/gh/RedisAI/JRedisAI/branch/master/graph/badge.svg?token=cC4H2TvQHs)](https://codecov.io/gh/RedisAI/JRedisAI)
[![Known Vulnerabilities](https://snyk.io/test/github/RedisAI/JRedisAI/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/RedisAI/JRedisAI?targetFile=pom.xml)


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
      <artifactId>jredisai</artifactId>
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

## Note

**Chunk size:** Since version `0.10.0`, the chunk size of model (blob) is set to 512mb (536870912 bytes) based on
default Redis configuration. This behavior can be changed by `redisai.blob.chunkSize` system property at the beginning
of the application. For example, chunk size can be limited to 8mb by setting `-Dredisai.blob.chunkSize=8388608` or
`System.setProperty(Model.BLOB_CHUNK_SIZE_PROPERTY, "8388608");`. A limit of 0 (zero) would disable chunking.

**Socket timeout:** Operations with large data and/or long processing time may require a higher socket timeout.
Following constructor may come in handy for that purpose.
```
  HostAndPort hostAndPort = new HostAndPort(host, port);
  JedisClientConfig clientConfig = DefaultJedisClientConfig.builder().socketTimeoutMillis(largeTimeout).build();
  new RedisAI(hostAndPort, clientConfig);
```
