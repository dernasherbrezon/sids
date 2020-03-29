# About [![Build Status](https://travis-ci.org/dernasherbrezon/sids.svg?branch=master)](https://travis-ci.org/dernasherbrezon/sids) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/ru.r2cloud/sids/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ru.r2cloud/sids)

Java implementation for Simple Downlink Share Convention (SiDS) protocol. 

 * More details could be found at [Amateur Radio – PEØSAT](http://www.pe0sat.vgnet.nl/decoding/tlm-decoding-software/sids/)
 * Python implementation located at [gr-satellites](https://github.com/daniestevez/gr-satellites/blob/master/python/submit.py) repository.

# Usage

1. Add maven dependency:

```xml
<dependency>
	<groupId>ru.r2cloud</groupId>
	<artifactId>sids</artifactId>
	<version>1.2</version>
</dependency>
```

2. Setup client and make a request:

```java
SidsClient client = new SidsClient("https://example.com", DEFAULT_TIMEOUT);
Telemetry cur = new Telemetry();
cur.setCallsign("your_call_sign");
cur.setFrame(new byte[]{ binary data });
cur.setLatitude(station.getLat());
cur.setLongitude(station.getLon());
cur.setNoradId("satellite_norad_id");
cur.setTimestamp(new Date());
client.send(cur);
```