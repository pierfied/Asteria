/**
 * Created by pierfiedorowicz on 2/24/17.
 */
public class Asteria {
    public static void main(String args[]) throws Exception{
        Cosmology cosmo = new FlatLambdaCDM(0.7,0.286);

        System.out.println("Loading Data");
        Galaxy gals[] = DataHandler.loadGals("data/redmagic.fit");
        Galaxy rands[] = DataHandler.loadRandoms("data/randoms_err.fit");

        System.out.println("Creating Catalogs");
        Catalog galCat = new Catalog(gals, cosmo);
        Catalog randCat = new Catalog(rands, cosmo);
    }
}
