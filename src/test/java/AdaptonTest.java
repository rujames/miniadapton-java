import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AdaptonTest {

    @Test
    void compute() {
        final DirectedComputationGraph dcg = new DirectedComputationGraph();
        final Adapton.Ref<Integer> r1 = dcg.ref(8);
        final Adapton.Ref<Integer> r2 = dcg.ref(10);
        final Adapton<Integer> a = dcg.expr((adapton) -> {
            adapton.addSubcomputation(r1);
            adapton.addSubcomputation(r2);
            return r1.compute() - r2.compute();
        });

        assertEquals(-2, a.compute());

        r1.set(2);
        assertEquals(-8, a.compute());
    }

    @Test
    void force() {
        final DirectedComputationGraph dcg = new DirectedComputationGraph();
        final Adapton.Ref<Integer> r = dcg.ref(5);
        final Adapton<Integer> a = dcg.expr(() -> (r.force() + 3));

        assertEquals(8, a.force());

        r.set(2);
        assertEquals(5, a.force());
    }

    @Test
    void state() {
        { // forcing a ref from within a ref
            final DirectedComputationGraph dcg = new DirectedComputationGraph();
            final Adapton.Ref<Integer> r1 = dcg.ref(2);
            final Adapton.Ref<Integer> r2 = dcg.ref(r1.force() + 4);
            final Adapton<Integer> a = dcg.expr(() -> r1.force() + r2.force());

            assertEquals(8, a.force());

            r1.set(10);
            assertEquals(16, a.force());
        }
        { // forcing a ref from within an expr
            final DirectedComputationGraph dcg = new DirectedComputationGraph();
            final Adapton.Ref<Integer> r1 = dcg.ref(2);
            final Adapton<Integer> r2 = dcg.expr(() -> r1.force() + 4);
            final Adapton<Integer> a = dcg.expr(() -> r1.force() + r2.force());

            assertEquals(8, a.force());

            r1.set(10);
            assertEquals(24, a.force());
        }
    }

}