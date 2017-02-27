/**
 * <h1>Density Map</h1>
 *
 * Created by pierfiedorowicz on 2/26/17.
 */
public class DensityMap extends AverageMap{
    OccupancyMap fMap;

    /**
     * Constructor
     *
     * @param cat         Catalog of Galaxy objects used to create map samples.
     * @param boundingBox Box of interest to work with.
     * @param numSamples  Number of samples to generate and average over.
     * @param fMap        OccupancyMap containing the percent occupancy of each voxel in boundingBox.
     */
    public DensityMap(Catalog cat, Box boundingBox, int numSamples, OccupancyMap fMap) {
        super(cat, boundingBox, numSamples);
        this.fMap = fMap;
    }

    @Override
    public double[][][] drawMapSample() {

        // Add each galaxy to a voxel.
        double N[][][] = new double[boundingBox.nx][boundingBox.ny][boundingBox.nz];
        for(int i = 0; i < cat.gals.length; i++){
            // Draw a Cartesian sample for the current galaxy.
            CartesianGalaxy cartSamp = cat.drawCartesianSample(i);

            // Calculate the index of the sampled point.
            int a = (int) ((cartSamp.x - boundingBox.x0)/boundingBox.voxLen);
            int b = (int) ((cartSamp.y - boundingBox.y0)/boundingBox.voxLen);
            int c = (int) ((cartSamp.z - boundingBox.z0)/boundingBox.voxLen);

            // Verify that the drawn point is within the box.
            if(a >= 0 && a < boundingBox.nx && b >= 0 && b < boundingBox.ny && c >= 0 && c < boundingBox.nz){
                N[a][b][c]++;
            }
        }

        // Update the number counts to account for the box occupancy and calculated expected number count.
        int numContributors = 0;
        double expectedN = 0;
        for(int i = 0; i < boundingBox.nx; i++){
            for(int j = 0; j < boundingBox.ny; j++){
                for(int k = 0; k < boundingBox.nz; k++){
                    N[i][j][k] /= fMap.map[i][j][k];

                    // Add to the expected number counts if f > 0.9.
                    if(fMap.map[i][j][k] > 0.9){
                        expectedN += N[i][j][k];
                        numContributors++;
                    }
                }
            }
        }
        expectedN /= numContributors;

        // Calculate y values and find maximum value of f.
        double maxf = 0;
        double y[][][] = new double[boundingBox.nx][boundingBox.ny][boundingBox.nz];
        for(int i = 0; i < boundingBox.nx; i++){
            for(int j = 0; j < boundingBox.ny; j++){
                for(int k = 0; k < boundingBox.nz; k++){
                    y[i][j][k] = Math.log(N[i][j][k]) - Math.log(expectedN);

                    // Update maxf as necessary.
                    if(fMap.map[i][j][k] > maxf) maxf = fMap.map[i][j][k];
                }
            }
        }



        return new double[0][][];
    }

    public double[] fitGaussian(){
        return null;
    }
}
