# Selenium-Screenshot
Tool to make screenshots of urls and compare them using ImageMagick via IM4Java.

## Prerequisites

* Add repository to **pom.xml** of your project:

```
<repositories>
    <repository>
        <snapshots>enabled</snapshots>
        <id>snapshots</id>
        <name>Snapshots</name>
        <url>.../nexus/content/repositories/snapshots/</url>
    </repository>
</repositories>
```
* Add Selenium and Selenium-Screenshot as Maven dependency:
```
<dependency>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>selenium-maven-plugin</artifactId>
    <version>2.3</version>
</dependency>

<dependency>
    <groupId>de.triology</groupId>
    <artifactId>selenium-screenshot</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
* Make sure you have [Chromedriver](https://sites.google.com/a/chromium.org/chromedriver/) or [Firefox Geckodriver](https://github.com/mozilla/geckodriver/releases) on your PATH.
* Install [ImageMagick](http://www.imagemagick.org/script/index.php).

## Getting Started

* Import selenium WebDriver and the Screenshooter into your Java Class:
```java
import org.openqa.selenium.*;
import de.triology.selenium_screenshot.Screenshooter; 
```
* Use Screenshooter as follows:
```java
public class SampleMain
{
    public static void main(String[] args)
    {
        // Make sure Chromedriver is on your PATH
        WebDriver webDriver = new ChromeDriver();
        webDriver.manage().window().maximize();

        Screenshooter sshot = new Screenshooter(webDriver);
        /* 
        *  On Windows ImageMagick won't be on your PATH by default after installation.
        *  You cann add it manually.
        *
        *  String ImageMagickPath = "C:\\Programs\\ImageMagick;C:\\Programs\\exiftool";
        *  Screenshooter sshot = new Screenshooter(webDriver, ImageMagickPath);
        */

        webDriver.get("http://im4java.sourceforge.net/install/index.html");

        sshot.makeScreenshot("screenshot/folder","screenshot01.png");

        webDriver.get("http://im4java.sourceforge.net/docs/index.html");

        sshot.makeScreenshot("screenshot/folder","screenshot02");

        sshot.compareImages("screenshot/folder","screenshot01","screenshot02.png");

        webDriver.quit();
    }
}
```

* Screenshot folders are relative to project working directory.
* Screenshot name can be ```"screenshot"``` or ```screenshot.png```.
* Images are saved as png files.

## Known Issues

* IM4Java as used in the Screenshooter can't compare two Images with different Dimensions
* Pages with scrollbars will be stuck together view by view (see [AShot framework](https://mvnrepository.com/artifact/ru.yandex.qatools.ashot/ashot/1.5.2)).
