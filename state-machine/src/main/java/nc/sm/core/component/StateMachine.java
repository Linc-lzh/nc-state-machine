package nc.sm.core.component;

public interface StateMachine<S, E> {
    S getCurrentState();
    void fire(E event);
    void fire(E event, Object context);
    void addTransition(Transition<S, E> transition);
    void addStateListener(StateMachineListener<S, E> listener);
}
