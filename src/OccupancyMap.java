/**
 * <h1>Occupancy Map</h1>
 * Map object that uses a random points catalog to calculate the occupancy percentage of each voxel.
 *
 * Created by pierfiedorowicz on 2/26/17.
 */
public class OccupancyMap extends AverageMap {
    // Expected randoms number counts density.
    double expectedn;

    // Bin width for calculating expected n.
    private static final double DELTA_Z = 0.01;

    /**
     * Constructor. Creates the occupancy map.
     *
     * @param rands       Catalog of random points to create map samples.
     * @param boundingBox Box of interest to work with.
     * @param numSamples  Number of samples to generate and average over.
     * @param Omega       Survey area in steradians.
     */
    public OccupancyMap(Catalog rands, Box boundingBox, int numSamples, double Omega) {
        super(rands, boundingBox, numSamples);

        calculateExpectedn(Omega);

        super.createAverageMap();
    }

    /**
     * Calculates the expected number count density of the random points.
     *
     * @param Omega Survey area in steradians.
     * @return Randoms expected number count density.
     */
    private double calculateExpectedn(double Omega){
        // Initialize the redshift bounds.
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        // Update the redshift bounds.
        for(int i = 0; i < cat.gals.length; i++){
            double z = cat.gals[i].zPhoto;
            if(z < minZ) minZ = z;
            if(z > maxZ) maxZ = z;
        }

        // Count the number of galaxies in each bucket.
        int numBuckets = (int) ((maxZ - minZ)/DELTA_Z);
        double N[] = new double[numBuckets];
        for(int i = 0; i < cat.gals.length; i++){
            // Get the redshift bin index of the current galaxy.
            int ind = (int) ((cat.gals[i].zPhoto - minZ)/DELTA_Z);

            // If the index is valid, update the number of galaxies in that redshift bin.
            if(ind >= 0 && ind < numBuckets){
                N[ind]++;
            }
        }

        // Calculate n and average over all bins.
        double n = 0;
        for(int i = 0; i < numBuckets; i++){
            // Find the midpoint of the bin.
            double midZ = minZ + (i + 0.5) * DELTA_Z;

            // Calculate the transverse comoving distance.
            double D_M = cat.cosmo.transverseComovingDist(midZ);
            n += N[i] / (D_M * D_M * Omega * cat.cosmo.differentialComDist(midZ, DELTA_Z));
        }
        expectedn = n / numBuckets;

        return expectedn;
    }

    @Override
    public double[][][] drawMapSample() {
        // Declare and initialize the sample array.
        double sample[][][] = new double[boundingBox.nx][boundingBox.ny][boundingBox.nz];

        // Add each random point to the appropriate voxel.
        for(int i = 0; i < cat.gals.length; i++){
            // Draw a Cartesian sample for the current galaxy.
            CartesianGalaxy cartSamp = cat.drawCartesianSample(i);

            // Calculate the index of the sampled point.
            int a = (int) ((cartSamp.x - boundingBox.x0)/boundingBox.voxLen);
            int b = (int) ((cartSamp.y - boundingBox.y0)/boundingBox.voxLen);
            int c = (int) ((cartSamp.z - boundingBox.z0)/boundingBox.voxLen);

            // Verify that the drawn point is within the box.
            if(a >= 0 && a < boundingBox.nx && b >= 0 && b < boundingBox.ny && c >= 0 && c < boundingBox.nz){
                sample[a][b][c]++;
            }
        }

        // Divide the number counts by the expected number counts to get the occupancy values.
        double expectedN = expectedn * Math.pow(boundingBox.voxLen,3);
        for(int i = 0; i < boundingBox.nx; i++){
            for(int j = 0; j < boundingBox.ny; j++){
                for(int k = 0; k < boundingBox.nz; k++){
                    sample[i][j][k] /= expectedN;
                }
            }
        }

        return sample;
    }
}
