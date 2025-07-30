package nc.sm.core.component;

public class Transition<S, E> {
    private final State<S, E> source;
    private final State<S, E> target;
    private final E event;
    private Action<S, E> action;
    private Guard guard;

    public Transition(State<S, E> source, State<S, E> target, E event) {
        this.source = source;
        this.target = target;
        this.event = event;
    }

    public Transition<S, E> withAction(Action<S, E> action) {
        this.action = action;
        return this;
    }

    public Transition<S, E> withGuard(Guard guard) {
        this.guard = guard;
        return this;
    }

    public State<S, E> getSource() {
        return source;
    }

    public State<S, E> getTarget() {
        return target;
    }

    public E getEvent() {
        return event;
    }

    public Action<S, E> getAction() {
        return action;
    }

    public Guard getGuard() {
        return guard;
    }
}
