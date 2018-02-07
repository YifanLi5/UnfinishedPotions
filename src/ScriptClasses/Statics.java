package ScriptClasses;

import java.util.Random;

public class Statics {

    public static final int CLEAN_RANARR = 257;
    public static final int VIAL_OF_WATER = 227;
    public static final int UNF_RANARR_POTION = 99;

    private Statics(){} //meant to be a constant provider, no constructor

    public static long randomNormalDist(double mean, double stddev){
        long debug = (long) ((new Random().nextGaussian() * stddev + mean));
        return Math.abs(debug); //in case we get a negative number
    }
}
