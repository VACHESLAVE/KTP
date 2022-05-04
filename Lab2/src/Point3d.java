public class Point3d {
    private double xCoord; //координата X
    private double yCoord; //координата Y
    private double zCoord; //координата Z

    public Point3d(double x, double y, double z) //Конструктор инициализации
    {
        xCoord = x;
        yCoord = y;
        zCoord = z;
    }

    public double distanceTo(Point3d p) //Расчет расстояния между точками
    {
        double xx = this.xCoord - p.xCoord;
        double yy = this.yCoord - p.yCoord;
        double zz = this.zCoord - p.zCoord;
        return (Math.sqrt(xx * xx + yy * yy + zz * zz));
    }

    public double getX() //Возвращение координаты X
    {
        return xCoord;
    }

    public double getY() //Возвращение координаты Y
    {
        return yCoord;
    }

    public double getZ() //Возвращение координаты Z
    {
        return zCoord;
    }
}
