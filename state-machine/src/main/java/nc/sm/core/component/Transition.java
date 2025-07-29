package nc.sm.core.component;

public class Transition<S, E> {
    private final State<S> source;
    private final State<S> target;
    private final E event;
    private Action<S, E> action;
    private Guard<E> guard;

    public Transition(State<S> source, State<S> target, E event) {
        this.source = source;
        this.target = target;
        this.event = event;
    }

    public Transition<S, E> withAction(Action<S, E> action) {
        this.action = action;
        return this;
    }

    public Transition<S, E> withGuard(Guard<E> guard) {
        this.guard = guard;
        return this;
    }

    public State<S> getSource() {
        return source;
    }

    public State<S> getTarget() {
        return target;
    }

    public E getEvent() {
        return event;
    }

    public Action<S, E> getAction() {
        return action;
    }

    public Guard<E> getGuard() {
        return guard;
    }
}
