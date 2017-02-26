/**
 * Created by pierfiedorowicz on 2/26/17.
 */
public abstract class AverageMap {
    // The array of average map values.
    double map[][][];

    // Reference catalog and bounding box.
    Catalog cat;
    Box boundingBox;

    // Sample number information.
    final int numSamples;
    private Integer sampleNum = 0;

    /**
     * Constructor. Creates the average map.
     *
     * @param cat Catalog of Galaxy objects used to create map samples.
     * @param boundingBox Box of interest to work with.
     * @param numSamples Number of samples to generate and average over.
     */
    public AverageMap(Catalog cat, Box boundingBox, int numSamples){
        this.cat = cat;
        this.boundingBox = boundingBox;
        this.numSamples = numSamples;

        // Initialize the double array.
        map = new double[boundingBox.nx][boundingBox.ny][boundingBox.nz];

        createAverageMap();
    }

    /**
     * Creates the average map in parallel using sample drawing threads.
     */
    private void createAverageMap(){
        // Get the number of cores.
        int numCores = Runtime.getRuntime().availableProcessors();

        // Create and start all of the map threads.
        Thread threads[] = new MapThread[numCores];
        for(int i = 0; i < numCores; i++){
            threads[i] = new MapThread();
            threads[i].start();
        }

        // Wait for all threads to finish.
        for(int i = 0; i < numCores; i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Divide each element by the number of samples.j
        for(int i = 0; i < boundingBox.nx; i++){
            for(int j = 0; j < boundingBox.ny; j++){
                for(int k = 0; k < boundingBox.nz; k++){
                    map[i][j][k] /= numSamples;
                }
            }
        }
    }

    /**
     * Abstract method that an extending class must implement to create new map samples.
     *
     * @return 3D Double Map Sample
     */
    public abstract double[][][] drawMapSample();

    /**
     * <h1>Map Thread</h1>
     * A thread class that draws new map samples in parallel and adds them to the average map array.
     */
    private class MapThread extends Thread{
        public void run(){
            while(true){
                // Get the current sample number.
                int curSamp;
                synchronized (sampleNum){
                    curSamp = sampleNum++;
                }

                // Check if done sampling.
                if(curSamp >= numSamples){
                    break;
                }

                // Draw a new sample.
                double sample[][][] = drawMapSample();

                // Add sample values to the average map.
                synchronized (map){
                    for(int i = 0; i < boundingBox.nx; i++){
                        for(int j = 0; j < boundingBox.ny; j++){
                            for(int k = 0; k < boundingBox.nz; k++){
                                map[i][j][k] += sample[i][j][k];
                            }
                        }
                    }
                }
            }
        }
    }
}
