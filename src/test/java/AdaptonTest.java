import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AdaptonTest {

    @Test
    void example() {
        final Adapton.Ref<Integer> r1 = Adapton.ref(8);
        final Adapton.Ref<Integer> r2 = Adapton.ref(10);
        final Adapton<Integer> a = Adapton.thunk((adapton) -> () -> {
            adapton.addSubcomputation(r1);
            adapton.addSubcomputation(r2);
            return r1.compute() - r2.compute();
        });

        assertEquals(-2, a.compute());

        r1.set(2);
        assertEquals(-8, a.compute());
    }
}