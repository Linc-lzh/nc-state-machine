package nc.sm.core.component;

@FunctionalInterface
public interface Guard<E> {
    boolean evaluate(Object context);
}
