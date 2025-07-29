package nc.sm.core.component;

public interface StateMachineListener<S, E> {
    void onTransitionComplete(S from, S to, E event, Object context);
    void onTransitionDenied(S from, E event, Object context);
}
