package nc.sm.core.component;

@FunctionalInterface
public interface Action<S, E> {
    void execute(S state, E event, Object context);
}
