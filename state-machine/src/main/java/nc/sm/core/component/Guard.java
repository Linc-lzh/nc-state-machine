package nc.sm.core.component;

@FunctionalInterface
public interface Guard {
    boolean evaluate(Object context);
}
