package ch.uzh.ifi.seal.soprafs19.entity;

import org.junit.Assert;
import org.junit.Test;

public class WorkerIdTest {

    private final WorkerId workerId = new WorkerId();

    @Test
    public void getAndSetPlayerId() {
        Assert.assertNull(workerId.getPlayerId());
        workerId.setPlayerId(1L);
        Assert.assertSame(workerId.getPlayerId(),1L);
    }

    @Test
    public void getAndSetWorkerNr() {
        Assert.assertNull(workerId.getWorkerNr());
        workerId.setWorkerNr(1);
        Assert.assertSame(workerId.getWorkerNr(),1);
    }
}
