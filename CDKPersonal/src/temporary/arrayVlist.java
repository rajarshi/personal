package temporary;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.templates.MoleculeFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Rajarshi Guha
 */


class Stopwatch {

    private long startTime = -1;
    private long stopTime = -1;
    private boolean running = false;

    public Stopwatch start() {
        startTime = System.currentTimeMillis();
        running = true;
        return this;
    }

    public Stopwatch stop() {
        stopTime = System.currentTimeMillis();
        running = false;
        return this;
    }

    /**
     * returns elapsed time in milliseconds
     * if the watch has never been started then
     * return zero
     */
    public long getElapsedTime() {
        if (startTime == -1) {
            return 0;
        }
        if (running) {
            return System.currentTimeMillis() - startTime;
        } else {
            return stopTime - startTime;
        }
    }

    public Stopwatch reset() {
        startTime = -1;
        stopTime = -1;
        running = false;
        return this;
    }
}

class ArrayBased {
    IAtomContainer[] array = new IAtomContainer[64];
    int multiplier = 2;
    int counter = 0;

    ArrayBased() {
        for (int i = 0; i < 60; i++) {
            array[i] = MoleculeFactory.makeAlphaPinene();
            counter++;
        }
    }

    public void add(IAtomContainer a) {
        if (counter % 64 == 0) {
            IAtomContainer[] tmp = new IAtomContainer[counter * multiplier];
            System.arraycopy(array, 0, tmp, 0, counter);
            array = new IAtomContainer[counter * multiplier];
            System.arraycopy(tmp, 0, array, 0, counter);
        }
        array[counter++] = a;
    }

    public IAtomContainer get(int pos) {
        if (pos >= counter) return null;
        else return array[pos];
    }

    public int getCount() {
        return counter;
    }
}

public class arrayVlist {

    public static void main(String[] args) {
        IAtomContainer ac = MoleculeFactory.makeBiphenyl();
        Random rng = new Random(123);



        System.out.println("Adding 50,000 molecules");

        Stopwatch timer = new Stopwatch().start();
        ArrayBased ab = new ArrayBased();
        for (int i = 0; i < 50000; i++) ab.add(ac);
        timer.stop();
        System.out.println(timer.getElapsedTime());

        timer.reset();
        timer.start();
        ListBased lb = new ListBased();
        for (int i = 0; i < 10000; i++) lb.add(ac);
        timer.stop();
        System.out.println(timer.getElapsedTime());

        int[] pos = new int[1000000];
        for (int i = 0; i < pos.length; i++) pos[i] = rng.nextInt(ab.getCount()-1);

        System.out.println("Getting a molecule 100,000 times");

        timer.reset();
        timer.start();
        for (int po : pos) {
            ac = ab.get(po);
        }
        timer.stop();
        System.out.println(timer.getElapsedTime());

        timer.reset();
        timer.start();
        for (int po : pos) {
            ac = lb.get(po);
        }
        timer.stop();
        System.out.println(timer.getElapsedTime());


    }

}
