/**
 * <h1>Cosmology Interface</h1>
 * This interface declares the minimum of operations that a cosmology object needs to have.
 *
 * Created by pierfiedorowicz on 2/24/17.
 */
public interface Cosmology {
    /**
     * Calculates the comoving distance from the redshift given the cosmology.
     *
     * @param z Redshift
     * @return Comoving Distance
     */
    public double comovingDist(double z);

    /**
     * Calculates the transverse comoving distance from the redshift given the cosmology.
     *
     * @param z Redshift
     * @return Transverse Comoving Distance
     */
    public double transverseComovingDist(double z);

    /**
     * Calculates the differential comoving difference with respect to delta-z.
     *
     * @param z Redshift
     * @param dz Delta-z
     * @return Delta-Dc
     */
    public double differentialComDist(double z, double dz);

    /**
     * Performs error propagation from error in z to error in comoving distance.
     *
     * @param z Redshift
     * @param zErr Redshift Error
     * @return Comoving Distance Error
     */
    public double comDistErr(double z, double zErr);

    /**
     * Calculates the redshift from the comoving distance given the cosmology.
     *
     * @param comDist Comoving Distance
     * @return Redshift
     */
    public double redshift(double comDist);
}
