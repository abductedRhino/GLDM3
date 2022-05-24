import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 Opens an image window and adds a panel below the image
 */
public class GRDM_U3 implements PlugIn {

    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;

    String[] items = {"Original", "Rot-Kanal", "Graustufen", "Negativ", "Binärbild", "Sepia", "AchtFarben"};


    public static void main(String args[]) {

        IJ.open("I:\\GLDM3_3\\src\\Bear.jpg");
        //IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

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
                        double u = 0;
                        double v = 0;

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

                        int rn = 255-(int) Math.round(luminanz + v / 0.877);
                        int bn = 255-(int) Math.round(luminanz + u / 0.493);
                        int gn = 255-(int) Math.round(1 / 0.587 * luminanz - 0.299 / 0.587 * rn - 0.114 / 0.587 * bn);

                        // Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
                        rn = limitRGB(rn);
                        gn = limitRGB(gn);
                        bn = limitRGB(bn);

                        pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                    }
                }
            }
            if (method.equals("Binärbild")) {

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

                        int rn = (int) Math.round(luminanz + v / 0.877);
                        int bn = (int) Math.round(luminanz + u / 0.493);
                        int gn = (int) Math.round(1 / 0.587 * luminanz - 0.299 / 0.587 * rn - 0.114 / 0.587 * bn);

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


    } // CustomWindow inner class
}
