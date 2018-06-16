package Util;

import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.util.Random;

public class Statics {

    public static final int CLEAN_HERB = 101; //ranarr = 257, toadflax = 2998, avantoe = 261
    public static final int VIAL_OF_WATER = 221;

    public static Script script;
    public static final boolean logNodes = false;

    private Statics(){} //meant to be a constant provider, no constructor

    public static long randomNormalDist(double mean, double stddev){
        long debug = (long) ((new Random().nextGaussian() * stddev + mean));
        return Math.abs(debug); //in case we get a negative number
    }

    public static void longRandomNormalDelay() throws InterruptedException {
        MethodProvider.sleep(randomNormalDist(2000,500));
    }

    public static void shortRandomNormalDelay() throws InterruptedException {
        MethodProvider.sleep(randomNormalDist(350,100));
    }
}
