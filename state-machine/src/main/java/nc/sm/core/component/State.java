package nc.sm.core.component;


public class State<S, E> {
    private final S state;
    private Action<S, E> entryAction;
    private Action<S, E> exitAction;

    public State(S state) {
        this.state = state;
    }

    public S getState() {
        return state;
    }

    public State<S, E> withEntryAction(Action<S, E> entryAction) {
        this.entryAction = entryAction;
        return this;
    }

    public State<S, E> withExitAction(Action<S, E> exitAction) {
        this.exitAction = exitAction;
        return this;
    }

    public Action<S, E> getEntryAction() {
        return entryAction;
    }

    public Action<S, E> getExitAction() {
        return exitAction;
    }
}
