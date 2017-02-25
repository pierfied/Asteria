import java.util.Random;

/**
 * <h1>Catalog</h1>
 * Contains a collection of Galaxy objects.
 *
 * Created by pierfiedorowicz on 2/24/17.
 */
public class Catalog {
    // Cosmology for the catalog.
    Cosmology cosmo;

    // Collection of Galaxy and CartesianGalaxy objects.
    Galaxy gals[];
    CartesianGalaxy cartNorms[];
    CartesianGalaxy cartSamps[];

    /**
     * Constructor
     *
     * @param gals Array of Galaxy objects.
     * @param cosmo Cosmology object.
     */
    public Catalog(Galaxy gals[], Cosmology cosmo){
        this.cosmo = cosmo;
        this.gals = gals;

        // Initialize the normals and samples arrays.
        cartNorms = new CartesianGalaxy[gals.length];
        cartSamps = new CartesianGalaxy[gals.length];

        // Create the normals and draw the initial Cartesian samples.
        createNorms();
        drawCartesianSamples();
    }

    /**
     * Calculate the components of the normal vector for each of the galaxies.
     */
    private void createNorms(){
        for(int i = 0; i < gals.length; i++){
            // Convert ra and dec into phi and theta (physics spherical coordinates).
            double phi = (Math.PI / 180.0) * gals[i].ra;
            double theta = Math.PI / 2.0 - (Math.PI / 180.0) * gals[i].dec;

            // Calculate the normal components.
            cartNorms[i].x = Math.sin(theta) * Math.cos(phi);
            cartNorms[i].y = Math.sin(theta) * Math.sin(phi);
            cartNorms[i].z = Math.cos(theta);
        }
    }

    /**
     * Draw a new Cartesian coordinate sample for each galaxy.
     */
    public void drawCartesianSamples(){
        // Get a new random number generator.
        Random rand = new Random();

        for(int i = 0; i < gals.length; i++){
            // Draw a new redshift sample.
            double zSamp = gals[i].zPhoto + gals[i].zErr * rand.nextGaussian();

            // Calculate the comoving distance of the sample.
            double comDist = cosmo.comovingDist(zSamp);

            // Calculate the Cartesian coordinates of the new sample.
            cartSamps[i].x = comDist * cartNorms[i].x;
            cartSamps[i].y = comDist * cartNorms[i].y;
            cartSamps[i].z = comDist * cartNorms[i].z;
        }
    }
}
