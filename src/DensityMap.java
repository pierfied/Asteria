import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

/**
 * <h1>Density Map</h1>
 * Average density map calculated in parallel using regularization.
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

        super.createAverageMap();
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

        // Update the number counts to account for the box occupancy, and calculate the expected number counts based
        // upon boxes with occupancy of greater than 0.9.
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

        // Calculate the density contrasts.
        double map[][][] = new double[boundingBox.nx][boundingBox.ny][boundingBox.nz];
        for(int i = 0; i < boundingBox.nx; i++){
            for(int j = 0; j < boundingBox.ny; j++){
                for(int k = 0; k < boundingBox.nz; k++){
                    map[i][j][k] = N[i][j][k] / expectedN - 1;
                }
            }
        }

        return map;
    }

    public void regularizeMap(){
        // Calculate the maximum occupancy value.
        double fMax = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < boundingBox.nx; i++){
            for(int j = 0; j < boundingBox.ny; j++){
                for(int k = 0; k < boundingBox.nz; k++){
                    if(fMap.map[i][j][k] > fMax) fMax = fMap.map[i][j][k];
                }
            }
        }

        // Create the bin indices.
        double fBinWidth = 0.1;
        int numBins = (int)Math.ceil(fMax/fBinWidth);

        // Create the yMap, and calculate the bin counts.
        double yMap[][][] = new double[boundingBox.nx][boundingBox.ny][boundingBox.nz];
        int binCounts[] = new int[numBins];
        for(int i = 0; i < boundingBox.nx; i++){
            for(int j = 0; j < boundingBox.ny; j++){
                for(int k = 0; k < boundingBox.nz; k++){
                    // Calculate the y values (log-normal).
                    yMap[i][j][k] = Math.log(1 + map[i][j][k]);

                    // Update the appropriate bin count.
                    int ind = (int)(fMap.map[i][j][k] / fBinWidth);
                    binCounts[ind]++;
                }
            }
        }

        // Create the bins.
        int binInds[] = new int[numBins];
        double bins[][] = new double[numBins][];
        for(int i = 0; i < numBins; i++){
            bins[i] = new double[binCounts[i]];
        }

        // Populate the bins.
        for(int i = 0; i < boundingBox.nx; i++){
            for(int j = 0; j < boundingBox.ny; j++){
                for(int k = 0; k < boundingBox.nz; k++){
                    // Get the appropriate bin.
                    int ind = (int)(fMap.map[i][j][k] / fBinWidth);

                    // Add the data to the bin.
                    bins[ind][binInds[ind]++] = yMap[i][j][k];
                }
            }
        }

        // Get the most likely bin with occupancy greater than 0.5
        int max = Integer.MIN_VALUE;
        int maxInd = 0;
        int highOccCount = 0;
        for(int i = 0; i < numBins; i++){
            if(i * fBinWidth < 0.5) continue;
            if(binCounts[i] > max){
                max = binCounts[i];
                highOccCount = max;
                maxInd = i;
            }else{
                highOccCount += binCounts[i];
            }
        }

        // Get the high occupancy y values.
        int ind = 0;
        double highOccY[] =  new double[highOccCount];
        for(int i = maxInd; i < numBins; i++){
            for(int j = 0; j < bins[i].length; j++){
                highOccY[ind++] = bins[i][j];
            }
        }

        // Calculate the overall mean for the y values and the corresponding regularization weight.
        double mean = calculateMean(highOccY);
        double meanRegWeight = 1 / calculateVar(highOccY, mean);
        System.out.println("Mean: " + mean);
        System.out.println("Mean Weight: " + meanRegWeight);

        // Calculate the regularization weight for each bin.
        double regWeights[] = new double[numBins];
        for(int i = 0; i < numBins; i++){
            regWeights[i] = 1 / calculateVar(bins[i], mean);
            System.out.println("Weight for f-bin " + i * fBinWidth + ": " + regWeights[i]);
        }

        // Regularize all of the density map values.
        for(int i = 0; i < boundingBox.nx; i++){
            for(int j = 0; j < boundingBox.ny; j++){
                for(int k = 0; k < boundingBox.nz; k++){
                    // Get the appropriate bin.
                    ind = (int)(fMap.map[i][j][k] / fBinWidth);

                    // Calculate the regularized y value.
                    if(fMap.map[i][j][k] > 0) {
                        yMap[i][j][k] = (regWeights[ind] * yMap[i][j][k] + meanRegWeight * mean)
                                / (regWeights[ind] + meanRegWeight);
                    }else{
                        // Set y to the mean if the occupancy is zero.
                        yMap[i][j][k] = mean;
                    }

                    // Calculate the regularized delta value.
                    map[i][j][k] = Math.exp(yMap[i][j][k]) - 1;
                }
            }
        }
    }

    private double calculateMean(double data[]){
        double sum = 0;
        int numValidPoints = 0;
        for(int i = 0; i < data.length; i++){
            // Verify that the data point is valid.
            if(Double.isInfinite(data[i]) || Double.isNaN(data[i])) continue;

            // Add the point to the sum.
            sum += data[i];
            numValidPoints++;
        }

        // Return the average.
        return sum/numValidPoints;
    }

    private double calculateVar(double data[], double mean){
        double sum = 0;
        int numValidPoints = 0;
        for(int i = 0; i < data.length; i++){
            // Verify that the data point is valid.
            if(Double.isInfinite(data[i]) || Double.isNaN(data[i])) continue;

            // Add the point to the sum.
            sum += Math.pow(data[i] - mean, 2);
            numValidPoints++;
        }

        // Return the unbiased variance estimate.
        return sum / (numValidPoints - 1);
    }
}
