package ro.bitgloss;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import lombok.Data;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@Data class Container { int integer; String string; }

@RunWith(JUnitQuickcheck.class)
public class PropertyOperationsQuickCheckTest {

    private Container container;
    private ro.bitgloss.Property<Container, String> strProp;
    private ro.bitgloss.Property<Container, Integer> intProp;

    @Before
    public void setUp() {
        container = new Container();
        strProp = ro.bitgloss.Property.of(
             Container::getString, Container::setString
        );
        intProp = ro.bitgloss.Property.of(
                Container::getInteger, Container::setInteger
        );
    }

    @Property(trials = 500)
    public void commutativeConcurrentTransformationsAreSafeForDifferentProperties(int n, String string) {
        PropertyOperations<Container> ops = new PropertyOperations<>();

        ops.set(strProp, string);
        ops.set(intProp, n);
        ops.transform(strProp, String::toUpperCase);
        ops.transform(intProp, i -> i + 1);
        ops.applyConcurrently(container);

        assertEquals(string.toUpperCase(), strProp.get(container).get());
        assertEquals(n + 1, intProp.get(container).get().intValue());
    }

    @Property(trials = 500)
    public void transformOperationsAreAppliedInOrder(int n) {
        PropertyOperations<Container> ops = new PropertyOperations<>();

        ops.transform(intProp, i -> n);
        ops.transform(intProp, i -> i++);
        ops.transform(intProp, i -> i++);
        ops.transform(intProp, i -> 3);
        ops.apply(container);

        assertEquals(3, intProp.get(container).get().intValue());
    }

    @Property(trials = 500)
    public void bulkTransformOperationsAreAppliedInOrder(int n) {
        PropertyOperations<Container> ops = new PropertyOperations<>();

        ops.transform(intProp, i -> n, i -> i++, i -> i++, i -> 3);
        ops.apply(container);

        assertEquals(3, intProp.get(container).get().intValue());
    }

    @Property(trials = 500)
    public void hookRunsForAllValuesAndSingleTransformations(int n) {
        PropertyOperations<Container> ops = new PropertyOperations<>();
        List<Integer> hookTrace = new ArrayList<>();

        ops.set(intProp, n);
        ops.transform(intProp, i -> i * i);
        ops.transform(intProp, i -> 2 * i);
        ops.applyWithHook(container, c -> hookTrace.add(c.getInteger()));

        assertEquals(asList(n, n * n, n * n * 2), hookTrace);
    }

    @Property(trials = 500)
    public void hookRunsForAllValuesAndOncePerBulkTransformation(int n) {
        PropertyOperations<Container> ops = new PropertyOperations<>();
        List<Integer> hookTrace = new ArrayList<>();

        ops.set(intProp, n);
        ops.transform(intProp, i -> i * i, i -> 2 * i);
        ops.transform(intProp, i -> i + 1, i -> i + 9);
        ops.applyWithHook(container, c -> hookTrace.add(c.getInteger()));

        assertEquals(asList(n, n * n * 2, n * n * 2 + 10), hookTrace);
    }

}