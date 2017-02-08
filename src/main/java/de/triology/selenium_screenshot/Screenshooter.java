package de.triology.selenium_screenshot;

import org.im4java.core.*;
import org.im4java.process.ArrayListErrorConsumer;
import org.im4java.process.ProcessStarter;
import org.openqa.selenium.WebDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by malte on 28.10.16.
 */
public class Screenshooter {

    /*
    * Getter and Setter
     */
    public WebDriver getWebDriver() {
        return webDriver;
    }

    public void setWebDriver(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public String getImageMagickPath() {
        return imageMagickPath;
    }

    public void setImageMagickPath(String imageMagickPath) {
        this.imageMagickPath = imageMagickPath;
    }

    private WebDriver webDriver;
    private String imageMagickPath;

    public Screenshooter(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public Screenshooter(WebDriver webDriver, String imageMagickPath) {
        this.webDriver = webDriver;
        this.imageMagickPath = imageMagickPath;
    }

    /*
    * Create new name of file if file name already exists.
    *
     */
    private String createNewNameInFolder(String old, File folder){
        String newStr = "";
        File[] children = folder.listFiles();
        int childlen = children.length;

        // Create new names depending on number of entries in folder.
        for(int i = 0; i < childlen; i++){
            newStr = old + i + ".png";
            int count = 0;

            // Compare new name with all existing names.
            for(File file : children){
                if(!file.getName().equals(newStr)){
                    count += 1;
                }
                if(count == childlen){
                    return newStr;
                }
            }
        }
        return old + childlen + ".png";
    }

    /*
    * Make the screenshot and create image file.
    *
     */
    private void screenshot(File file){
        System.out.println("Screenshot: Creating screenshot: " + file.getName() + ".");
        Screenshot screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
                .takeScreenshot(webDriver);
        try {
            ImageIO.write(screenshot.getImage(), "PNG", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function controlling the proper generation of screenshots.
     *
     * <code>.png</code> extension is assumed if not specified.
     *
     * @param screenshotFolderName Folder to save a screenshot
     * @param screenshotFileName filename for screenshot
     */
    public void makeScreenshot(String screenshotFolderName, String screenshotFileName) {

        if(!screenshotFileName.endsWith(".png")){
            screenshotFileName += ".png";
        }

        File screenshotFolder = new File(screenshotFolderName);
        File screenshotFile = new File(screenshotFolder, screenshotFileName);

        // Ordner existiert noch nicht
        if (!screenshotFolder.exists()) {
            System.out.println("Screenshot: Creating directory: " + screenshotFolderName);
            if (screenshotFolder.mkdirs()) {
                System.out.println("Screenshot: Directory " + screenshotFolderName + " created.");
            }
        }
        else
        {
            System.out.println("Screenshot: Directory '" + screenshotFolder.getAbsolutePath() + "' exists.");
        }

        if (screenshotFolder.exists() && !screenshotFile.exists()) {
            screenshot(screenshotFile);
        }
        else if (screenshotFile.exists())
        {
            String screenshotFileName_diff = screenshotFileName.substring(0,screenshotFileName.length() - 4);
            screenshotFileName_diff = createNewNameInFolder(screenshotFileName_diff,screenshotFolder);
            System.out.println("Screenshot: File with name '" + screenshotFileName + "' allready exists.\n" +
                    "Rename to " + screenshotFileName_diff + ".");
            screenshot(new File(screenshotFolder, screenshotFileName_diff));
        }
    }

    /**
     * Function to compare two images.
     *
     * @return number of pixels that differ between images (<code>-1</code> if they are identical),
     * or <code>-1</code> if comparison failed.
     */
    public int compareImages(String directory, String screenshot, String screenshotCompare){
        int result = -1; // -1 is returned unless compare is called successfully

        if(!screenshot.endsWith(".png")){
            screenshot += ".png";
        }
        if(!screenshotCompare.endsWith(".png")){
            screenshotCompare += ".png";
        }

        File dir = new File(directory);
        File scShot = new File(dir, screenshot);
        File scShotCompare = new File(dir, screenshotCompare);

        if(!dir.exists()){
            System.out.println("Compare: Directory " + directory + " is not valid.");
        }
        else if (dir.listFiles().length < 1 ){
            System.out.println("Compare: Directory " + directory + " is empty.");
        }
        else if (!scShot.exists() || !scShotCompare.exists()){
            System.out.println("Compare: At least one screenshot doesn't exist in " + directory +".");
        }
        else{
            System.out.println("Compare: Compare " + scShot.getName() + " with " + scShotCompare.getName() + ".");
            ProcessStarter.setGlobalSearchPath(imageMagickPath);

            try {
                Info scShotInfo = new Info(scShot.getAbsolutePath(),true);

                System.out.println("Compare: Width of " + screenshot + ": " + scShotInfo.getImageWidth());

                Info scShotCompareInfo = new Info(scShotCompare.getAbsolutePath(),true);
                System.out.println("Compare: Width of " + screenshotCompare + ": " + scShotCompareInfo.getImageWidth());

            } catch (InfoException e) {
                e.printStackTrace();
            }

            CompareCmd com_cmd = new CompareCmd() {
                @Override
                protected void finished(int code) throws Exception {
                    if (code > 0) {
                        if (getErrorText() != null) {
                            super.finished(code);
                        }
                    } else {
                        super.finished(code);
                    }
                }
            };
            IMOperation op = new IMOperation();

            op.addRawArgs("-metric", "AE");
            op.addImage(scShot.getAbsolutePath());
            op.addImage(scShotCompare.getAbsolutePath());

            ArrayListErrorConsumer errorConsumer = new ArrayListErrorConsumer();
            com_cmd.setErrorConsumer(errorConsumer);

            File comparisonOutput = new File(dir, "output.png");
            op.addImage(comparisonOutput.getAbsolutePath());

            try {
                com_cmd.run(op);

                List<String> errorOutput = errorConsumer.getOutput();
                if (!errorOutput.isEmpty()) {
                    String resultString = errorOutput.get(0);
                    try {
                        result = Integer.parseInt(resultString);
                    } catch (NumberFormatException e) {
                        System.err.println("Comparison error: " + resultString);
                    }
                } else {
                    result = 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IM4JavaException e) {
                CommandException ce = (CommandException) e;
                int ce_code = ce.getReturnCode();

                if(ce_code == 2) {
                    e.printStackTrace();
                }
                else if(ce_code == 1){
                    System.out.println("Differences between images found.");
                }
            }
        }
        return result;
    }
}
