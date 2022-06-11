import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

class Adapton<T> {

    public static class Ref<T> extends Adapton<T> {
        Ref(DirectedComputationGraph dcg, T value, boolean isClean) {
            super(dcg, (adapton) -> adapton.result, isClean);
            result = value;
        }

        public void set(T value) {
            result = value;
            dirty();
        }
    }

    final DirectedComputationGraph dcg;
    final Function<Adapton<T>, T> computation;
    final Set<Adapton> subcomputations;
    final Set<Adapton> supercomputations;
    T result;
    boolean isClean;

    Adapton(DirectedComputationGraph dcg, Function<Adapton<T>, T> computation, boolean isClean) {
        this.dcg = dcg;
        this.computation = computation;
        this.subcomputations = new HashSet<>();
        this.supercomputations = new HashSet<>();
        this.result = null;
        this.isClean = isClean;
    }

    public T force() {
        final Optional<Adapton> previouslyAdapting = dcg.currentlyAdapting;
        dcg.currentlyAdapting = Optional.of(this);
        final T result = compute();
        dcg.currentlyAdapting = previouslyAdapting;
        dcg.currentlyAdapting.ifPresent((adapton) -> {
            adapton.addSubcomputation(this);
        });
        return result;
    }

    <U> void addSubcomputation(Adapton<U> other) {
        this.subcomputations.add(other);
        other.supercomputations.add(this);
    }

    <U> void removeSubcomputation(Adapton<U> other) {
        this.subcomputations.remove(other);
        other.supercomputations.remove(this);
    }

    T compute() {
        if (isClean) {
            return result;
        } else {
            new HashSet<>(subcomputations).forEach(this::removeSubcomputation);
            isClean = true;
            result = computation.apply(this);
            return compute();
        }
    }

    void dirty() {
        if (isClean) {
            isClean = false;
            supercomputations.forEach(Adapton::dirty);
        }
    }

}
