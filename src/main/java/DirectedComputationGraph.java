import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class DirectedComputationGraph {

    Optional<Adapton> currentlyAdapting = Optional.empty();

    public <T> Adapton<T> expr(Function<Adapton<T>, T> computation) {
        return new Adapton<>(this, computation, false);
    }

    public <T> Adapton<T> expr(Supplier<T> computation) {
        return new Adapton<>(this, (adapton) -> computation.get(), false);
    }

    public <T> Adapton.Ref<T> ref(T value) {
        return new Adapton.Ref<>(this, value, true);
    }
}
