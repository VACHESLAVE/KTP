import java.util.Scanner;

public class Lab2 {
    public static void main(String[] args) {

        Scanner px1 = new Scanner(System.in);//Координата x первой точки
        Scanner py1 = new Scanner(System.in);//Координата y первой точки
        Scanner pz1 = new Scanner(System.in);//Координата z первой точки
        double x1 = px1.nextDouble(), y1 = py1.nextDouble(), z1 = pz1.nextDouble();

        Scanner px2 = new Scanner(System.in);//Координата x второй точки
        Scanner py2 = new Scanner(System.in);//Координата y второй точки
        Scanner pz2 = new Scanner(System.in);//Координата z второй точки
        double x2 = px2.nextDouble(), y2 = py2.nextDouble(), z2 = pz2.nextDouble();

        Scanner px3 = new Scanner(System.in);//Координата x третьей точки
        Scanner py3 = new Scanner(System.in);//Координата y третьей точки
        Scanner pz3 = new Scanner(System.in);//Координата z третьей точки
        double x3 = px3.nextDouble(), y3 = py3.nextDouble(), z3 = pz3.nextDouble();

        Point3d p1 = new Point3d(x1, y1, z1);//Создание первой точки
        Point3d p2 = new Point3d(x2, y2, z2);//Создание второй точки
        Point3d p3 = new Point3d(x3, y3, z3);//Создание третьей точки

        if (checkPoints(p1, p2, p3)) //Проверка условия с выводом
            System.out.printf("Площадь треугольника: " + "%.2f", computeArea(p1, p2, p3));
        else
            System.out.println("Одна из точек повторяется");
    }

    public static double computeArea(Point3d p1, Point3d p2, Point3d p3) //Вычисление площади треугольника
    {
        double a = p1.distanceTo(p2); //Вычисление стороны a
        double b = p2.distanceTo(p3); //Вычисление стороны b
        double c = p3.distanceTo(p1); //Вычисление стороны c
        double per = ((a + b + c) / 2); //Вычисление периметра
        return (Math.sqrt(per * (per - a) * (per - b) * (per - c))); //Площадь треугольника по Формуле Герона
    }

    public static boolean checkPoints(Point3d p1, Point3d p2, Point3d p3) //Проверка на совпадение точек
    {
        if (((p1.getX() == p2.getX()) && (p1.getY() == p2.getY()) && (p1.getZ() == p2.getZ())) ||
                ((p2.getX() == p3.getX()) && (p2.getY() == p3.getY()) && (p2.getZ() == p3.getZ())) ||
                ((p1.getX() == p3.getX()) && (p1.getY() == p3.getY()) && (p1.getZ() == p3.getZ())))
            return false;
        return true;
    }
}