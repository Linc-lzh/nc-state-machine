package nc.sm.core.component;


public class State<S> {
    private final S state;
    private Action<S, ?> entryAction;
    private Action<S, ?> exitAction;

    public State(S state) {
        this.state = state;
    }

    public S getState() {
        return state;
    }

    public State<S> withEntryAction(Action<S, ?> entryAction) {
        this.entryAction = entryAction;
        return this;
    }

    public State<S> withExitAction(Action<S, ?> exitAction) {
        this.exitAction = exitAction;
        return this;
    }

    public Action<S, ?> getEntryAction() {
        return entryAction;
    }

    public Action<S, ?> getExitAction() {
        return exitAction;
    }
}
