package ro.bitgloss;

import de.c.seiler.simpleoptics.OptionalLens;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Represents a property (or part) of type B inside a data structure (or whole) of type A.
 * The property is represented as a first class citizen and can be
 * composed with other properties, providing better modularity and reduced context
 * for mutation.
 *
 * @param <A> the whole (data structure)
 * @param <B> the part (field)
 */
public class Property<A, B> {

    private OptionalLens<A, B> lens;

    private Property(Function<A, B> get, BiFunction<A, B, A> set) {
        lens = new OptionalLens<>(toOptional(get), toOptional(set));
    }

    private Property(OptionalLens<A, B> lens) {
        this.lens = lens;
    }

    /**
     * Constructs a new property based on the get/set operations
     * @param get
     * @param set
     * @param <A>
     * @param <B>
     * @return Property
     */
    public static <A, B> Property<A, B> of(Function<A, B> get, BiConsumer<A, B> set) {
        return new Property<>(get, (a, b) -> { set.accept(a, b); return a;});
    }

    /**
     * Mutates the the property on the given object (whole) using the mutator.
     * @param whole
     * @param mutator
     * @return Optional whole
     */
    public Optional<A> mutate(A whole, UnaryOperator<B> mutator) {
        return lens.mod(whole, ob -> Optional.ofNullable(mutator.apply(ob.orElse(null))));
    }

    /**
     * @param whole
     * @return Optional property value
     */
    public Optional<B> get(A whole) {
        return lens.get(whole);
    }

    /**
     * Compose this property with the other property.
     * Useful for accessing nested properties in data structures.
     * @param other
     * @param <C>
     * @return Composed Property
     */
    public <C> Property<A, C> to(Property<B, C> other) {
        return new Property<>(lens.andThen(other.lens));
    }

    private BiFunction<Optional<A>, Optional<B>, Optional<A>> toOptional(BiFunction<A,B,A> f) {
        return (oa, ob) -> oa.map(a -> {ob.map(b -> f.apply(a, b)); return a;});
    }

    private Function<Optional<A>, Optional<B>> toOptional(Function<A,B> f) {
        return oa -> oa.map(f);
    }
}
