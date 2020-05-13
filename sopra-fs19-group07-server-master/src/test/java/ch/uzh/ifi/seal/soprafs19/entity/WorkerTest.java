package ch.uzh.ifi.seal.soprafs19.entity;

import org.junit.Assert;
import org.junit.Test;

public class WorkerTest {

    private final Worker worker = new Worker();

    @Test
    public void getAndSetField() {
        Field field = new Field();
        Assert.assertNull(worker.getField());
        worker.setField(field);
        Assert.assertNotNull(worker.getField());
        Assert.assertEquals(worker.getField(), field);
    }

    @Test
    public void getAndSetPlayer() {
        Player player = new Player();
        Assert.assertNull(worker.getPlayer());
        worker.setPlayer(player);
        Assert.assertNotNull(worker.getPlayer());
        Assert.assertEquals(worker.getPlayer(), player);
    }

    @Test
    public void getAndSetWorkerNr() {
        Assert.assertNull(worker.getWorkerNr());
        worker.setWorkerNr(11);
        Assert.assertNotNull(worker.getWorkerNr());
        Assert.assertSame(worker.getWorkerNr(), 11);
    }
}
