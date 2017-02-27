import java.util.Random;

/**
 * <h1>Catalog</h1>
 * Contains a collection of Galaxy objects.
 *
 * Created by pierfiedorowicz on 2/24/17.
 */
public class Catalog {
    // Cosmology for the catalog.
    final Cosmology cosmo;

    // Collection of Galaxy and CartesianGalaxy objects.
    final Galaxy gals[];
    final CartesianGalaxy cartNorms[];

    // Random number generator for the class.
    Random rand = new Random();

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

        // Create the normals and draw the initial Cartesian samples.
        createNorms();
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
            double x = Math.sin(theta) * Math.cos(phi);
            double y = Math.sin(theta) * Math.sin(phi);
            double z = Math.cos(theta);

            // Create the CartesianGalaxy object.
            cartNorms[i] = new CartesianGalaxy(x,y,z);
        }
    }


    /**
     * Draw a new Cartesian redshift sample for the galaxy with the given id. Returns null for invalid id.
     *
     * @param id Index of the galaxy of interest.
     * @return CartesianGalaxy object containing the cooridnates of the redshift sample.
     */
    public CartesianGalaxy drawCartesianSample(int id){
        // Verify that the id is valid.
        if(id < 0 || id >= gals.length){
            return null;
        }

        // Draw a new redshift sample.
        double zSamp = gals[id].zPhoto + gals[id].zErr * rand.nextGaussian();

        // Calculate the comoving distance of the sample.
        double comDist = cosmo.comovingDist(zSamp);

        // Calculate the Cartesian coordinates of the new sample.
        double x = comDist * cartNorms[id].x;
        double y = comDist * cartNorms[id].y;
        double z = comDist * cartNorms[id].z;

        return new CartesianGalaxy(x,y,z);
    }

    /**
     * Create a box that completely contains all points inside of the catalog +/- 5 sigma.
     *
     * @param voxLen Length of each side of the voxel.
     * @return Box object that contains all points in the catalog +/- 5 sigma.
     */
    public Box createBoundingBox(double voxLen){
        // Initialize the bounds.
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;

        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        // Loop through each galaxy and update the bounds as necessary.
        for(int i = 0; i < cartNorms.length; i++){
            // Get the comoving distance and comoving distance error for the photo-z value.
            double photoR = cosmo.comovingDist(gals[i].zPhoto);
            double rErr = cosmo.comDistErr(gals[i].zPhoto, gals[i].zErr);

            // Calculate the x,y,z cooridnates for 5 sigma from photo-z inwards.
            double closeR = photoR - 5 * rErr;
            double closeX = closeR * cartNorms[i].x;
            double closeY = closeR * cartNorms[i].y;
            double closeZ = closeR * cartNorms[i].z;

            // Update the bounds as necessary.
            if(closeX < minX) minX = closeX;
            if(closeY < minY) minY = closeY;
            if(closeZ < minZ) minZ = closeZ;

            if(closeX > maxX) maxX = closeX;
            if(closeY > maxY) maxY = closeY;
            if(closeZ > maxZ) maxZ = closeZ;

            // Calculate the x,y,z cooridnates for 5 sigma from photo-z outwards.
            double farR = photoR + 5 * rErr;
            double farX = farR * cartNorms[i].x;
            double farY = farR * cartNorms[i].y;
            double farZ = farR * cartNorms[i].z;

            // Update the bounds as necessary.
            if(farX < minX) minX = farX;
            if(farY < minY) minY = farY;
            if(farZ < minZ) minZ = farZ;

            if(farX > maxX) maxX = farX;
            if(farY > maxY) maxY = farY;
            if(farZ > maxZ) maxZ = farZ;
        }

        // Create the bounding box object.
        int nx = (int) Math.ceil((maxX - minX)/voxLen);
        int ny = (int) Math.ceil((maxY - minY)/voxLen);
        int nz = (int) Math.ceil((maxZ - minZ)/voxLen);
        return new Box(minX,minY,minZ,nx,ny,nz,voxLen);
    }
}
