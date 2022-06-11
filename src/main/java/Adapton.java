import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class Adapton<T> {

    static Optional<Adapton> currentlyAdapting = Optional.empty();

    final Function<Adapton<T>, T> computation;
    final Set<Adapton> subcomputations;
    final Set<Adapton> supercomputations;
    T result;
    boolean isClean;

    private Adapton(Function<Adapton<T>, T> computation, boolean isClean) {
        this.computation = computation;
        this.subcomputations = new HashSet<>();
        this.supercomputations = new HashSet<>();
        this.result = null;
        this.isClean = isClean;
    }

    public static <T> Adapton<T> expr(Function<Adapton<T>, T> computation) {
        return new Adapton<>(computation, false);
    }

    public static <T> Adapton<T> expr(Supplier<T> computation) {
        return new Adapton<>((adapton) -> computation.get(), false);
    }

    public static <T> Adapton.Ref<T> ref(T value) {
        return new Adapton.Ref<>(value, true);
    }

    public <U> void addSubcomputation(Adapton<U> other) {
        this.subcomputations.add(other);
        other.supercomputations.add(this);
    }

    public <U> void removeSubcomputation(Adapton<U> other) {
        this.subcomputations.remove(other);
        other.supercomputations.remove(this);
    }

    public T compute() {
        if (isClean) {
            return result;
        } else {
            new HashSet<>(subcomputations).forEach(this::removeSubcomputation);
            isClean = true;
            result = computation.apply(this);
            return compute();
        }
    }

    public T force() {
        final Optional<Adapton> previouslyAdapting = currentlyAdapting;
        currentlyAdapting = Optional.of(this);
        final T result = compute();
        currentlyAdapting = previouslyAdapting;
        currentlyAdapting.ifPresent((adapton) -> {
            adapton.addSubcomputation(this);
        });
        return result;
    }

    public void dirty() {
        if (isClean) {
            isClean = false;
            supercomputations.forEach(Adapton::dirty);
        }
    }

    public static class Ref<T> extends Adapton<T> {
        private Ref(T value, boolean isClean) {
            super((adapton) -> adapton.result, isClean);
            result = value;
        }

        public void set(T value) {
            result = value;
            dirty();
        }
    }
}
