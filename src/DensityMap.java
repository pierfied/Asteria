import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
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

        // Update the number counts to account for the box occupancy, calculated expected number count, and max f value.
        int numContributors = 0;
        double expectedN = 0;
        double maxf = 0;
        for(int i = 0; i < boundingBox.nx; i++){
            for(int j = 0; j < boundingBox.ny; j++){
                for(int k = 0; k < boundingBox.nz; k++){
                    N[i][j][k] /= fMap.map[i][j][k];

                    // Add to the expected number counts if f > 0.9.
                    if(fMap.map[i][j][k] > 0.9){
                        expectedN += N[i][j][k];
                        numContributors++;
                    }

                    // Update maxf as necessary.
                    if(fMap.map[i][j][k] > maxf) maxf = fMap.map[i][j][k];
                }
            }
        }
        expectedN /= numContributors;

        // Calculate y values and create the bucket counts.
        double df = 0.1;
        int numBins = (int) Math.ceil(maxf / df);
        int bucketCounts[] = new int[numBins];
        double y[][][] = new double[boundingBox.nx][boundingBox.ny][boundingBox.nz];
        for(int i = 0; i < boundingBox.nx; i++){
            for(int j = 0; j < boundingBox.ny; j++){
                for(int k = 0; k < boundingBox.nz; k++){
                    y[i][j][k] = Math.log(N[i][j][k]) - Math.log(expectedN);

                    // Calculate which bucket this y sample belongs to and increase the count.
                    int bucketInd = (int) (fMap.map[i][j][k] / df);
                    bucketCounts[bucketInd]++;
                }
            }
        }

        // Allocate and initialize the sample arrays for each bucket.
        double samps[][] = new double[numBins][];
        for(int i = 0; i < numBins; i++){
            samps[i] = new double[bucketCounts[i]];
        }

        // Add each y sample to the appropriate bucket.
        int inds[] = new int[numBins];
        for(int i = 0; i < boundingBox.nx; i++){
            for(int j = 0; j < boundingBox.ny; j++){
                for(int k = 0; k < boundingBox.nz; k++){
                    // Calculate which bucket this y sample belongs to and add it to the bucket array/
                    int bucketInd = (int) (fMap.map[i][j][k] / df);
                    samps[bucketInd][inds[bucketInd]++] = y[i][j][k];
                }
            }
        }

        // Calculate sigma for each bucket.
        double sigmas[] = new double[numBins];
        for(int i = 0; i < numBins; i++){
            double fit[] = fitGaussian(samps[i],20);
            sigmas[i] = fit[2];
        }

        return y;
    }

    public double[] fitGaussian(double samps[], int numBins){
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < samps.length; i++){
            if(samps[i] < min && !Double.isInfinite(samps[i])) min = samps[i];
            if(samps[i] > max && !Double.isInfinite(samps[i])) max = samps[i];
        }

        double bins[] = new double[numBins];
        double binWidth = (max - min)/numBins;
        for(int i = 0; i < samps.length; i++){
            if(Double.isInfinite(samps[i])) continue;

            int bin = (int) ((samps[i] - min)/binWidth);

            samps[bin]++;
        }

        WeightedObservedPoints obs = new WeightedObservedPoints();
        for(int i = 0; i < numBins; i++){
            double midBinVal = min + (i + 0.5) * binWidth;
            obs.add(midBinVal, bins[i]);
        }

        return GaussianCurveFitter.create().fit(obs.toList());
    }
}
