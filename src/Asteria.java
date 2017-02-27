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

        System.out.println("Creating Bounding Box");
        double voxLen = 20;
        Box box = galCat.createBoundingBox(voxLen);
        System.out.println("Box " + box.x0 + " " + box.y0 + " " + box.z0 + " " + box.nx + " " + box.ny + " " + box.nz);

        System.out.println("Creating f-Map");
        OccupancyMap fMap = new OccupancyMap(randCat,box,10,Math.PI);
    }
}
