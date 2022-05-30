import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

import javax.swing.JComboBox;

import static java.lang.Math.round;
import static java.util.Arrays.fill;

/**
 Opens an image window and adds a panel below the image
 */
public class GRDM_U3 implements PlugIn {

    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;

    String[] items = {"Original", "Rot-Kanal", "Graustufen", "Negativ", "Binärbild", "3-Graustufen",
            "7-Graustufen", "VertikalerDither", "Sepia", "AchtFarben"};


    public static void main(String args[]) {

        IJ.open("C:\\Users\\natha\\OneDrive\\Documents\\HTW GDM SS 2022\\Uebungen\\GLDM3\\src\\Bear.jpg");
        IJ.open("C:\\Users\\to0o\\GLDM3\\src\\Bear.jpg");

        GRDM_U3 pw = new GRDM_U3();
        pw.imp = IJ.getImage();
        pw.run("");
    }

    public void run(String arg) {
        if (imp==null)
            imp = WindowManager.getCurrentImage();
        if (imp==null) {
            return;
        }
        CustomCanvas cc = new CustomCanvas(imp);

        storePixelValues(imp.getProcessor());

        new CustomWindow(imp, cc);
    }


    private void storePixelValues(ImageProcessor ip) {
        width = ip.getWidth();
        height = ip.getHeight();

        origPixels = ((int []) ip.getPixels()).clone();
    }


    class CustomCanvas extends ImageCanvas {

        CustomCanvas(ImagePlus imp) {
            super(imp);
        }

    } // CustomCanvas inner class


    class CustomWindow extends ImageWindow implements ItemListener {

        private String method;

        CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }

        void addPanel() {
            //JPanel panel = new JPanel();
            Panel panel = new Panel();

            JComboBox cb = new JComboBox(items);
            panel.add(cb);
            cb.addItemListener(this);

            add(panel);
            pack();
        }

        public void itemStateChanged(ItemEvent evt) {

            // Get the affected item
            Object item = evt.getItem();

            if (evt.getStateChange() == ItemEvent.SELECTED) {
                System.out.println("Selected: " + item.toString());
                method = item.toString();
                changePixelValues(imp.getProcessor());
                imp.updateAndDraw();
            }

        }


        private void changePixelValues(ImageProcessor ip) {

            // Array zum Zurückschreiben der Pixelwerte
            int[] pixels = (int[])ip.getPixels();

            if (method.equals("Original")) {

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;

                        pixels[pos] = origPixels[pos];
                    }
                }
            }

            if (method.equals("Rot-Kanal")) {

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        //int g = (argb >>  8) & 0xff;
                        //int b =  argb        & 0xff;

                        int rn = r;
                        int gn = 0;
                        int bn = 0;


                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
                        rn = limitRGB(rn);
                        gn = limitRGB(gn);
                        bn = limitRGB(bn);

                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }
            if (method.equals("Graustufen")) {

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >>  8) & 0xff;
                        int b =  argb        & 0xff;

                        double luminanz = 0.299 *  r + 0.587 *  g + 0.114 *  b;

                        int rn = (int) luminanz;
                        int bn = (int) luminanz;
                        int gn = (int) luminanz;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
                        rn = limitRGB(rn);
                        gn = limitRGB(gn);
                        bn = limitRGB(bn);

                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }
            if (method.equals("Negativ")) {

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >>  8) & 0xff;
                        int b =  argb        & 0xff;

                        double luminanz = 0.299 *  r + 0.587 *  g + 0.114 *  b;
                        double u = (b - luminanz) * 0.493;
                        double v = (r - luminanz) * 0.877;

                        int rn = 255-(int) round(luminanz + v / 0.877);
                        int bn = 255-(int) round(luminanz + u / 0.493);
                        int gn = 255-(int) round(1 / 0.587 * luminanz - 0.299 / 0.587 * rn - 0.114 / 0.587 * bn);

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
                        rn = limitRGB(rn);
                        gn = limitRGB(gn);
                        bn = limitRGB(bn);

                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }
            if (method.equals("Binärbild")) {

                double[] colors = new double[2];
                colors = fillArrayWith(colors);

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >>  8) & 0xff;
                        int b =  argb        & 0xff;

                        int luminint = (int) round(0.299 *  r + 0.587 *  g + 0.114 *  b);


                        //  luminint dem nächsten der werte zuordnen.
                        //System.out.print(luminint + "   ");
                        luminint = closestValue(luminint, colors);
                        //System.out.println(luminint);



                        int rn = luminint;
                        int bn = luminint;
                        int gn = luminint;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }
            if (method.equals("3-Graustufen")) {

                double[] colors = new double[3];
                colors = fillArrayWith(colors);

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >>  8) & 0xff;
                        int b =  argb        & 0xff;

                        int luminint = (int) round(0.299 *  r + 0.587 *  g + 0.114 *  b);


                        //  luminint dem nächsten der werte zuordnen.
                        //System.out.print(luminint + "   ");
                        luminint = closestValue(luminint, colors);
                        //System.out.println(luminint);



                        int rn = luminint;
                        int bn = luminint;
                        int gn = luminint;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }
            if (method.equals("7-Graustufen")) {

                double[] colors = new double[7];
                colors = fillArrayWith(colors);

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >>  8) & 0xff;
                        int b =  argb        & 0xff;

                        int luminint = (int) round(0.299 *  r + 0.587 *  g + 0.114 *  b);

                        //  luminint dem nächsten der werte zuordnen.
                        //System.out.print(luminint + "   ");
                        luminint = closestValue(luminint, colors);
                        //System.out.println(luminint);



                        int rn = luminint;
                        int bn = luminint;
                        int gn = luminint;

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }
            if (method.equals("VertikalerDither")) {

                int[] lastLine = new int[width];
                fill(lastLine, 0);

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y * width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        double luminanz = 0.299 * r + 0.587 * g + 0.114 * b;
                        int luminint = (int)round(luminanz);

                        int displayColor = luminint + lastLine[x];

                        if (displayColor < 128) {
                            luminint = 0;
                            lastLine[x] = displayColor - 0;
                        } else {
                            luminint = 255;
                            lastLine[x] = displayColor - 255;
                        }

                        int rn = luminint;
                        int bn = luminint;
                        int gn = luminint;



                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
                        rn = limitRGB(rn);
                        gn = limitRGB(gn);
                        bn = limitRGB(bn);

                        pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
                    }
                }
            }
            if (method.equals("Sepia")) {

                for (int y=0; y<height; y++) {
                    for (int x=0; x<width; x++) {
                        int pos = y*width + x;
                        int argb = origPixels[pos];  // Lesen der Originalwerte

                        int r = (argb >> 16) & 0xff;
                        int g = (argb >>  8) & 0xff;
                        int b =  argb        & 0xff;

                        double luminanz = 0.299 *  r + 0.587 *  g + 0.114 *  b;
                        double u = (b - luminanz) * 0.493;
                        double v = (r - luminanz) * 0.877;

                        int rn = (int) (luminanz*1.3);
                        int bn = (int) (luminanz*0.8);
                        int gn = (int) (luminanz*1.2);


                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
                        rn = limitRGB(rn);
                        gn = limitRGB(gn);
                        bn = limitRGB(bn);

                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }
        }
        private int limitRGB(int RGB) {
            if (RGB > 255) {
                RGB = 255;
            }
            if (RGB < 0) {
                RGB = 0;
            }
            return RGB;
        }
        private double[] fillArrayWith(double[] colors) {
            for (int i = 0; i < colors.length; i++) {
                colors[i] = (255 / ((double)colors.length - 1)) * i;

            }
            System.out.println(Arrays.toString(colors));
            return colors;
        }
        private int closestValue(int num, double[] numbers) {
            int ret = 0;
            double closest = Integer.MAX_VALUE;
            for (int i = 0; i < numbers.length; i++) {
                double distance = (Math.max(numbers[i], num)) - (Math.min(numbers[i], num));
                if (distance < closest) {
                    closest = distance;
                    ret = (int) Math.round(numbers[i]);
                }
            }
            return ret;
        }
    } // CustomWindow inner class
}
