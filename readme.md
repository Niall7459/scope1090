## Scope1090

![scope1090 in action](./scope1090control.gif)

Features:
- Scaling the scope viewport
- Rotation of the viewport
- Connection to SBS data stream

Equipment:
- ADS-B/Mode-S decoder with SBS output
- A suitable 1090MHz antenna

*Scope1090 requires a separate program to perform message decoding from aircraft. Scope1090 is compatible with programs that support an SBS output stream*

Running scope1090:

```java -jar scope1090-1.0-SNAPSHOT-shaded.jar --lat=(your latitude) --lon=(your longitude) --connect=127.0.0.1:30003```

You may specify multiple servers as such:

*This is useful if you also need to connect to an MLAT server feedback port.*

```java -jar scope1090-1.0-SNAPSHOT-shaded.jar --lat=(your latitude) --lon=(your longitude) --connect=127.0.0.1:30003 --connect=127.0.0.1:31003```

Controls:
- Mouse wheel: Zoom in/out
- Mouse wheel + ctrl: Rotate scope clockwise/anti-clockwise
