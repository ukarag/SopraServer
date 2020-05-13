package ch.uzh.ifi.seal.soprafs19.entity;

import org.junit.Assert;
import org.junit.Test;

public class FieldTest {

    private Field field = new Field();

    @Test
    public void getSetAndAddLevel() {
        field = new Field();
        Assert.assertSame(field.getLevel(), 0);
        field.setLevel(3);
        Assert.assertSame(field.getLevel(), 3);
        field.addLevel();
        Assert.assertSame(field.getLevel(), 4);
        field.addLevel();
        Assert.assertSame(field.getLevel(), 4);
    }

    @Test
    public void addDome() {
        field = new Field();
        Assert.assertSame(field.getLevel(), 0);
        field.addDome();
        Assert.assertSame(field.getLevel(), 40);
        field.setLevel(3);
        field.addDome();
        Assert.assertSame(field.getLevel(), 4);
    }

    @Test
    public void getAndSetClickable() {
        Assert.assertFalse(field.getClickable()[0]);
        Assert.assertFalse(field.getClickable()[1]);
        field.setClickable(1, true);
        Assert.assertTrue(field.getClickable()[0]);
        Assert.assertFalse(field.getClickable()[1]);
    }

    @Test
    public void getAndSetWorker() {
        Worker worker = new Worker();
        Assert.assertNull(field.getWorker());
        field.setWorker(worker);
        Assert.assertNotNull(field.getWorker());
        Assert.assertEquals(field.getWorker(), worker);
        Assert.assertEquals(field, worker.getField());
        field.setWorker(null);
        Assert.assertNull(worker.getField());
        Assert.assertNull(field.getWorker());
        field.setWorker(null);
        Assert.assertNull(worker.getField());
        Assert.assertNull(field.getWorker());
    }

    @Test
    public void getAndSetGame() {
        Game game = new Game();
        Assert.assertNull(field.getGame());
        field.setGame(game);
        Assert.assertNotNull(field.getGame());
    }

    @Test
    public void getXY() {
        field = new Field(2,2);
        Assert.assertSame(field.getX(), 2);
        Assert.assertSame(field.getY(), 2);
    }

    @Test
    public void equals() {
        field = new Field(1, 1);
        Field fieldSame = field;
        field.setLevel(1);
        Assert.assertEquals(field, fieldSame);
        Assert.assertEquals(field.hashCode(), fieldSame.hashCode());
        Field field2 = new Field(1, 2);
        Assert.assertNotEquals(field, field2);
        Field field3 = new Field(2, 1);
        Assert.assertNotEquals(field, field3);
        Field field4 = new Field(1, 1);
        Assert.assertEquals(field, field4);
        Assert.assertNotEquals(field, 1);
        Assert.assertNotEquals(field, null);
    }
}
