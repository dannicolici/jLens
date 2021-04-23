package ro.bitgloss;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class PropertyQuickCheckTest {

    @Property
    public void getBackWhatYouPutIn(Integer n) {
        Queue<Integer> queue = new LinkedList<>();
        ro.bitgloss.Property<Queue<Integer>, Integer> intProp = ro.bitgloss.Property
                .of(Queue::peek, Queue::add);

        assertSame(intProp.get(intProp.mutate(queue, i -> n).get()).get(),
                n);
    }

    @Property
    public void puttingBackWhatYouGotDoesNotChangeTheProperty(Integer n) {
        Queue<Integer> queue = new LinkedList<>(Collections.singleton(n));
        ro.bitgloss.Property<Queue<Integer>, Integer> intProp = ro.bitgloss.Property
                .of(Queue::peek, Queue::add);

        assertSame(intProp.mutate(queue, i -> intProp.get(queue).get()).get().peek(),
                n);
    }

    @Property
    public void settingTwiceIsTheSameAsSettingOnce(Integer n) {
        Queue<Integer> queue = new LinkedList<>();
        ro.bitgloss.Property<Queue<Integer>, Integer> intProp = ro.bitgloss.Property
                .of(Queue::peek, Queue::add);

        assertSame(intProp.mutate(intProp.mutate(queue, i -> n).get(), i -> n).get().peek(),
                intProp.mutate(queue, i -> n).get().peek());
    }

}