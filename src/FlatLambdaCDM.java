import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

/**
 * <h1>FlatLambdaCDM</h1>
 * A Cosmology object implementing a Lambda-CDM model with no curvature (Omega_k = 0).
 *
 * Created by pierfiedorowicz on 2/24/17.
 */
public class FlatLambdaCDM implements Cosmology {
    // Cosmological parameters.
    public final double h, omegaM, omegaLambda, dh;

    // Interpolator functions to calculate redshift to distance and visa versa.
    private UnivariateFunction zTOr;
    private UnivariateFunction rTOz;

    // Interpolation and integration bounds/settings.
    private static final double MIN_Z = 0;
    private static final double MAX_Z = 10;
    private static final double DELTA_Z = 0.001;

    /**
     * Constructs the object, sets the cosmological parameters of the object and sets up the interpolators
     * between redshift and comoving distance.
     *
     * @param h Dimensionless Hubble parameter.
     * @param omegaM Omega matter value.
     */
    public FlatLambdaCDM(double h, double omegaM){
        this.h = h;
        this.omegaM = omegaM;
        this.omegaLambda = 1 - omegaM;
        this.dh = 3000/h; // Hubble Distance

        setupInterpolators();
    }

    /**
     * Sets up the interpolators to calculate redshift from distance and visa versa for the given cosmology.
     */
    private void setupInterpolators(){
        // Integrate and calculate the values of the comoving distance.
        int numSteps = (int)((MAX_Z - MIN_Z)/DELTA_Z) + 1;
        double redshift[] = new double[numSteps];
        double comovingDist[] = new double[numSteps];
        for(int i = 1; i < numSteps; i++){
            // Set the current redshift value.
            redshift[i] = i * DELTA_Z;

            // Calculate the comoving distance at the current redshift.
            comovingDist[i] = comovingDist[i-1] + dh * DELTA_Z / E(redshift[i]);
        }

        // Setup the interpolators.
        UnivariateInterpolator interpolator = new LinearInterpolator();
        zTOr = interpolator.interpolate(redshift,comovingDist);
        rTOz = interpolator.interpolate(comovingDist,redshift);
    }

    /**
     * Calculates the value of E(z).
     *
     * @param z Redshift
     * @return E(z)
     */
    private double E(double z){
        return Math.sqrt(omegaM * Math.pow(1 + z, 3) + omegaLambda);
    }

    @Override
    public double comovingDist(double z) {
        return zTOr.value(z);
    }

    @Override
    public double comDistErr(double z, double zErr) {
        return zErr * dh / E(z);
    }

        @Override
    public double redshift(double comDist) {
        return rTOz.value(comDist);
    }
}