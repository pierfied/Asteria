import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.TableHDU;

import java.io.IOException;

/**
 * <h1>Data Handler</h1>
 * Handles all data importing. All methods here are completely dependent upon a specific data format.
 *
 * Created by pierfiedorowicz on 2/27/17.
 */
public class DataHandler {
    /**
     * Reads all galaxies in from a fit file.
     *
     * @param fname Filename to read from.
     * @return Galaxy Array
     * @throws FitsException
     * @throws IOException
     */
    public static Galaxy[] loadGals(String fname) throws FitsException, IOException {
        // Open the file.
        Fits f = new Fits(fname);

        // Load the data.
        TableHDU tab = (TableHDU) f.getHDU(1);
        double ra[] = (double[]) tab.getColumn(1);
        double dec[] = (double[]) tab.getColumn(2);
        float zPhoto[] = (float[]) tab.getColumn(11);
        float zErr[] = (float[]) tab.getColumn(12);
        float zSpec[] = (float[]) tab.getColumn(14);

        // Create the array of galaxies.
        Galaxy gals[] = new Galaxy[ra.length];
        for(int i = 0; i < gals.length; i++){
            gals[i] = new Galaxy(ra[i],dec[i],zSpec[i],zPhoto[i],zErr[i]);
        }

        return gals;
    }

    /**
     * Reads all randoms in from a fit file.
     *
     * @param fname Filename to read from.
     * @return Galaxy Array
     * @throws FitsException
     * @throws IOException
     */
    public static Galaxy[] loadRandoms(String fname) throws FitsException, IOException {
        // Open the file.
        Fits f = new Fits(fname);

        // Load the data.
        TableHDU tab = (TableHDU) f.getHDU(1);
        double ra[] = (double[]) tab.getColumn(0);
        double dec[] = (double[]) tab.getColumn(1);
        float zPhoto[] = (float[]) tab.getColumn(2);
        float zErr[] = (float[]) tab.getColumn(4);

        // Create the array of galaxies.
        Galaxy gals[] = new Galaxy[ra.length];
        for(int i = 0; i < gals.length; i++){
            gals[i] = new Galaxy(ra[i],dec[i],zPhoto[i],zPhoto[i],zErr[i]);
        }

        return gals;
    }
}
