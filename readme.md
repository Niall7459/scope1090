## Scope1090

scope1090 is a simple swing app inspired by the old-fashioned radars seen in movies and games, written in Kotlin. Being written in a JVM compatible language allows for high portability between multiple operating systems and does not require any compilation to run.

### Features
- Fading effect on the dots
- Set the speed of the rotating line
- Zoom the view in/out
- Rotate the view

### Installation
**Windows Installation**
1. Download Java 8 or newer from https://www.java.com/en/download/
2. Download the latest scope1090 jar archive from the Releases tab
3. Open a command prompt in the same location you downloaded scope1090
4. Run `java -jar scope1090-1.0-SNAPSHOT-shaded.jar --lat=(your latitude) --lon=(your longitude)` to start scope1090

**Linux, Raspberry Pi**
1. Download Java 8 or newer by running `apt-get install default-jre`
2. Download the latest scope1090 jar archive from the Releases tab
3. Open a terminal window and navigate to where scope1090 is located
4. Run `java -jar scope1090-1.0-SNAPSHOT-shaded.jar --lat=(your latitude) --lon=(your longitude)` to start scope1090

**MacOS / OSX**
1. Download Java 8 or newer from https://www.java.com/en/download/
2. Download the latest scope1090 jar archive from the Releases tab
3. Open a terminal window and navigate to where scope1090 is located
4. Run `java -jar scope1090-1.0-SNAPSHOT-shaded.jar --lat=(your latitude) --lon=(your longitude)` to start scope1090

Remember to check the version you have downloaded, you will need to adjust the command for this. These examples use `1.0-SNAPSHOT`.

scope1090 is not fully tested on a retina display and may render blurry or pixelated.

### Performance
Scope1090 will work well on any modern computer. Where applicable, Improvements have been put in place to increase performance on low-end devices such as a raspberry pi, for example, text, range markers, and the gauge, are all cached between repaints.

### Customise Options
You may customise how the app looks using the following command line arguments, an example:
```
java -jar scope1090-1.1-SNAPSHOT-shaded.jar --lat=0.0 --lon=-0.0 --sbs=127.0.0.1:30003 --rpm=30
```

| Option  | Description                                                                       |
|---------|-----------------------------------------------------------------------------------|
| --lat   | Latitude of center of scope (required)                                            |
| --lon   | Longitude of center of scope (required)                                           |
| --sbs   | Define a SBS data source connection in the format `--sbs=127.0.0.1:30003` |
| --adsbx | Use an ADSBX API Key `--adsbx=(your api key)`               |
| --hd    | Enable high quality rendering hints
| --rpm   | Revolutions per minute of the rotating line. Default: 36. Set to 0 to disable sweep |
| --fade  | Set the amount of seconds for a dot to disappear after being passed |                  
| --color | The colour (expressed as a hexadecimal, #00FF00) to use for the display |
| --markers | Distance (in kilometres) of the range markers. Default: 50. Set to 0 to disable |
| --nocursor | Remove the north-facing dashed cursor |
| --nobuttons | Disable the left and right buttons |
| --fullscreen | Open's the window in fullscreen |

**Example**
```
java -jar scope1090-1.1-SNAPSHOT-shaded.jar --lat=0.0 --lon=-0.0 --sbs=127.0.0.1:30003 --hd --fade=2 --markers=10 --fullscreen --nocursor --nobuttons
```

You may disable the rotating line by setting `--rpm=0` as a command line argument. The fade will be based on the time elapsed since a dot last moved. Disable the fading effect altogether using: `--fade=0`

### Controls
| Action             | Description                                               |
|--------------------|-----------------------------------------------------------|
| Mouse Wheel        | Increase or decrease the range of the viewport            |
| Mouse Wheel + CTRL | Rotate the view clockwise/anti-clockwise   |

### Notice
This is not a real radar, it is simply a graphical interface built to look like the ones you see in movies, it will not show anything by default unless you input it with some data.