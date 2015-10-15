import java.util.Collection;

/**
 * @author korektur
 *         15/10/2015
 */
public class Maintainer implements Runnable {

    private final Collection<Thread> threads;

    public Maintainer(Collection<Thread> threads) {
        this.threads = threads;
    }


    @Override
    public void run() {

    }
}
