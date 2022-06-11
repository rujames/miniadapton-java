import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class Adapton<T> {
    final Supplier<T> thunk;
    T result;
    final Set<Adapton> subcomputations;
    final Set<Adapton> supercomputations;
    boolean isClean;

    private Adapton(Function<Adapton<T>, Supplier<T>> makeThunk, boolean isClean) {
        this.thunk = makeThunk.apply(this);
        this.result = null;
        this.subcomputations = new HashSet();
        this.supercomputations = new HashSet<>();
        this.isClean = isClean;
    }

    public static <T> Adapton<T> thunk(Function<Adapton<T>, Supplier<T>> makeSupplier) {
        return new Adapton<>(makeSupplier, false);
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
            ImmutableSet.copyOf(subcomputations).forEach(this::removeSubcomputation);
            isClean = true;
            result = thunk.get();
            return compute();
        }
    }

    public void dirty() {
        if (isClean) {
            isClean = false;
            supercomputations.forEach(Adapton::dirty);
        }
    }

    public static class Ref<T> extends Adapton<T> {
        private Ref(T value, boolean isClean) {
            super((adapton) -> () -> adapton.result, isClean);
            result = value;
        }

        public void set(T value) {
            result = value;
            dirty();
        }
    }
}
