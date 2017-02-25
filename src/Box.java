/**
 * Created by pierfiedorowicz on 2/25/17.
 */
public class Box {
    // Origin of the box.
    double x0,y0,z0;

    // Number of voxels in each direction.
    double nx,ny,nz;

    // Length of each voxel in one direction.
    double voxLen;

    /**
     * Constructor
     *
     * @param x0 x coordinate of the origin.
     * @param y0 y coordinate of the origin.
     * @param z0 z coordinate of the origin.
     * @param nx Number of voxels in x direction.
     * @param ny Number of voxels in y direction.
     * @param nz Number of voxels in z direction.
     * @param voxLen Length of each voxel in one direction.
     */
    public Box(double x0, double y0, double z0, double nx, double ny, double nz, double voxLen){
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
        this.voxLen = voxLen;
    }
}
