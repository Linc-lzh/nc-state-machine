package nc.sm.core.component;

import nc.sm.biz.file.exception.FileProcessException;
import nc.sm.biz.file.pojo.FileProcessContext;

public interface StateMachine<S, E> {
    S getCurrentState();
    void fire(E event) throws FileProcessException;
    void fire(E event, FileProcessContext context) throws FileProcessException;
    void addTransition(Transition<S, E> transition);
    void addStateListener(StateMachineListener<S, E> listener);
}
