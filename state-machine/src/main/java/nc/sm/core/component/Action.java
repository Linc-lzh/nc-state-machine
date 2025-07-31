package nc.sm.core.component;

import nc.sm.biz.file.exception.FileProcessException;
import nc.sm.biz.file.pojo.FileProcessContext;

@FunctionalInterface
public interface Action<S, E> {
    void execute(S state, E event, FileProcessContext context) throws FileProcessException;
}
