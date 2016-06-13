package edu.illinois.mitra.starl.objects;

/**
 * Created by SC on 6/3/16.
 * Adapted from http://www.anddev.org/other-coding-problems-f5/using-java-awt-polygon-in-android-t6521.html
 * Class created to take place of java.awt.Polygon
 */

/**
 * Minimum Polygon class for Android.
 */
public class Polygon
{
    // Polygon coodinates.
    private int[] polyY, polyX;

    // Number of sides in the polygon.
    private int polySides;

    /**
     * Default constructor.
     * @param px Polygon y coods.
     * @param py Polygon x coods.
     * @param ps Polygon sides count.
     */
    public Polygon( int[] px, int[] py, int ps )
    {
        polyX = px;
        polyY = py;
        polySides = ps;
    }

    /**
     * Checks if the Polygon contains a point.
     * @see "http://alienryderflex.com/polygon/"
     * @param x Point horizontal pos.
     * @param y Point vertical pos.
     * @return Point is in Poly flag.
     */
    public boolean contains( int x, int y )
    {
        boolean oddTransitions = false;
        for( int i = 0, j = polySides -1; i < polySides; j = i++ )
        {
            if( ( polyY[ i ] < y && polyY[ j ] >= y ) || ( polyY[ j ] < y && polyY[ i ] >= y ) )
            {
                if( polyX[ i ] + ( y - polyY[ i ] ) / ( polyY[ j ] - polyY[ i ] ) * ( polyX[ j ] - polyX[ i ] ) < x )
                {
                    oddTransitions = !oddTransitions;
                }
            }
        }
        return oddTransitions;
    }
}

