/**
 * <h1>Galaxy</h1>
 * Class for galaxies and random points.
 *
 * Created by pierfiedorowicz on 2/24/17.
 */
public class Galaxy {
    // Values associated with the point.
    double ra;
    double dec;
    double zSpec;
    double zPhoto;
    double zErr;

    /**
     * Constructor
     *
     * @param ra Right Ascension
     * @param dec Declination
     * @param zSpec Spectroscopic Redshift
     * @param zPhoto Photometric Redshift
     * @param zErr Photometric Redshift Error
     */
    Galaxy(double ra, double dec, double zSpec, double zPhoto, double zErr){
        this.ra = ra;
        this.dec = dec;
        this.zSpec = zSpec;
        this.zPhoto = zPhoto;
        this.zErr = zErr;
    }
}
