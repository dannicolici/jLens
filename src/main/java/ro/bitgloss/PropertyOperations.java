package ro.bitgloss;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;

/**
 * Opinionated API meant to provide guidance in working with {@link Property} objects.
 * @param <A> the whole (data structure)
 */
public class PropertyOperations<A> {

    private final ArrayList setList = new ArrayList();
    private final ArrayList opsList = new ArrayList();

    /**
     * Sets a value on a given property
     */
    public <B> void set(Property<A, B> prop, B val) {
        Objects.requireNonNull(prop);

        UnaryOperator<B> op = b -> val;
        setList.add(new Object[]{prop, op});
    }

    /**
     * Transform property with multiple operations
     * @see PropertyOperations#transform(Property, UnaryOperator)
     */
    public <B> void transform(Property<A, B> prop, UnaryOperator<B>... operators) {
        Objects.requireNonNull(operators);

        transform(prop, asList(operators));
    }

    /**
     * Transform property with multiple operations
     * @see PropertyOperations#transform(Property, UnaryOperator)
     */
    public <B> void transform(Property<A, B> prop, List<UnaryOperator<B>> operators) {
        Objects.requireNonNull(operators);

        operators.stream()
                .reduce((op1, op2) -> a -> op2.apply(op1.apply(a)))
                .ifPresent(op -> transform(prop, op));
    }

    /**
     * Transform property with a single operation. The application of the  operation
     * is not performed here, but rather when one of these ae called:
     * {@link #apply(Object)}, {@link #applyConcurrently(Object)},
     * {@link #applyWithHook(Object, Consumer)}, {@link #applyWithHookConcurrently(Object, Consumer)}
     */
    public <B> void transform(Property<A, B> prop, UnaryOperator<B> operator) {
        Objects.requireNonNull(prop);
        Objects.requireNonNull(operator);

        opsList.add(new Object[]{prop, operator});
    }

    /**
     * Applies sequentially the predefined values and transformations on the provided object.
     * @param whole
     */
    public A apply(A whole) {
        setList.forEach(doExecute(whole));
        opsList.forEach(doExecute(whole));

        return whole;
    }
    /**
     * Same as {@link #apply(Object)} with the addition of running the hook for each
     * individual value and transformation application.
     * @param whole
     */
    public A applyWithHook(A whole, Consumer<A> hook) {
        setList.forEach(doExecuteWithHook(whole, hook));
        opsList.forEach(doExecuteWithHook(whole, hook));

        return whole;
    }

    /**
     * Applies sequentially the predefined values and in parallel the transformations
     * on the provided object.
     * @param whole
     */
    public A applyConcurrently(A whole) {
        setList.forEach(doExecute(whole));
        opsList.parallelStream().forEach(doExecute(whole));

        return whole;
    }

    /**
     * Same as {@link #applyConcurrently(Object)} with the addition of running the hook for
     * each individual value and single transformation application.
     * @param whole
     */
    public A applyWithHookConcurrently(A whole, Consumer<A> hook) {
        setList.forEach(doExecuteWithHook(whole, hook));
        opsList.parallelStream().forEach(doExecuteWithHook(whole, hook));

        return whole;
    }

    private Consumer doExecute(A whole) {
        return pair -> fst(pair).mutate(whole, snd(pair));
    }

    private Consumer doExecuteWithHook(A whole, Consumer<A> hook) {
        return pair -> {
            fst(pair).mutate(whole, snd(pair));
            hook.accept(whole);
        };
    }

    private Property fst(Object pair) {
        return (Property) ((Object[]) pair)[0];
    }

    private UnaryOperator snd(Object pair) {
        return (UnaryOperator) ((Object[]) pair)[1];
    }
}
