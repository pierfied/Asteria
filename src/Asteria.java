import java.io.PrintWriter;

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
        OccupancyMap fMap = new OccupancyMap(randCat,box,10,0.47774743094);

        PrintWriter out = new PrintWriter("occ.csv");
        for(int i = 0; i < box.nx; i++){
            for(int j = 0; j < box.ny; j++){
                for(int k = 0; k < box.nz; k++){
                    double x = box.x0 + (i + 0.5) * box.voxLen;
                    double y = box.x0 + (j + 0.5) * box.voxLen;
                    double z = box.x0 + (k + 0.5) * box.voxLen;

                    out.println(x + "," + y + "," + z + "," + fMap.map[i][j][k]);
                }
            }
        }
        out.close();
    }
}
