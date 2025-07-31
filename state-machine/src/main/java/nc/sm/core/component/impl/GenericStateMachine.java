package nc.sm.core.component.impl;

import nc.sm.biz.file.config.StateMachineConfig;
import nc.sm.biz.file.entity.FileProcessState;
import nc.sm.biz.file.exception.FileProcessException;
import nc.sm.biz.file.pojo.FileProcessContext;
import nc.sm.core.component.StateMachine;
import nc.sm.core.component.StateMachineListener;
import nc.sm.core.component.Transition;
import nc.sm.core.component.pojo.RetryPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class GenericStateMachine<S, E> implements StateMachine<S, E> {
    private S currentState;
    private final StateMachineConfig config;
    private final Map<S, Map<E, Transition<S, E>>> transitionMap = new ConcurrentHashMap<>();
    private final List<StateMachineListener<S, E>> listeners = new ArrayList<>();

    public GenericStateMachine(S initialState, StateMachineConfig config) {
        this.config = config;
        this.currentState = initialState;
    }

    @Override
    public S getCurrentState() {
        return currentState;
    }

    @Override
    public void fire(E event) throws FileProcessException {
        fire(event, null);
    }

    @Override
    public void fire(E event, FileProcessContext context) throws FileProcessException {
        Map<E, Transition<S, E>> eventTransitions = transitionMap.get(currentState);
        if (eventTransitions == null) {
            throw new IllegalStateException("No transitions defined for state: " + currentState);
        }

        Transition<S, E> transition = eventTransitions.get(event);
        if (transition == null) {
            throw new IllegalStateException("No transition for event: " + event + " in state: " + currentState);
        }

        // 执行守卫条件检查
        if (transition.getGuard() != null && context != null && !transition.getGuard().evaluate(context)) {
            notifyTransitionDenied(currentState, event, context);
            return;
        }

        // 执行退出动作
        if (transition.getSource().getExitAction() != null) {
            transition.getSource().getExitAction().execute(currentState, event, context);
        }

        // 执行转换动作
        if (transition.getAction() != null) {
            // 执行状态处理器
            if (transition.isAsync()) {
                executeAsync(transition, event, context);
            } else {
                executeWithRetry(transition, event, context);
            }
        }

        S oldState = currentState;
        currentState = transition.getTarget().getState();

        // 执行进入动作
        if (transition.getTarget().getEntryAction() != null) {
            transition.getTarget().getEntryAction().execute(currentState, event, context);
        }

        notifyTransitionComplete(oldState, currentState, event, context);
    }

    @Override
    public void addTransition(Transition<S, E> transition) {
        transitionMap
                .computeIfAbsent(transition.getSource().getState(), k -> new HashMap<>())
                .put(transition.getEvent(), transition);
    }

    @Override
    public void addStateListener(StateMachineListener<S, E> listener) {
        listeners.add(listener);
    }

    private void notifyTransitionComplete(S from, S to, E event, Object context) {
        for (StateMachineListener<S, E> listener : listeners) {
            listener.onTransitionComplete(from, to, event, context);
        }
    }

    private void notifyTransitionDenied(S from, E event, Object context) {
        for (StateMachineListener<S, E> listener : listeners) {
            listener.onTransitionDenied(from, event, context);
        }
    }

    private void executeWithRetry(Transition<S, E> transition, E event, FileProcessContext context) throws FileProcessException {
        RetryPolicy retryPolicy = transition.getRetryPolicy();
        int attempts = 0;

        while (true) {
            try {
                attempts++;
                transition.getAction().execute(currentState, event, context);
                return;
            } catch (FileProcessException e) {
                if (attempts >= retryPolicy.getMaxAttempts()) {
                    throw e;
                }

                try {
                    Thread.sleep(retryPolicy.getBackoffPeriod());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new FileProcessException("Interrupted during retry", ie);
                }
            }
        }
    }

    private void executeAsync(Transition<S, E> handler, E event, FileProcessContext context) {
        // 使用线程池执行异步处理
        CompletableFuture.runAsync(() -> {
            try {
                executeWithRetry(handler, event,  context);
            } catch (Exception e) {
                System.out.println("Async state handling failed");
                context.addLog("ASYNC_ERROR", "Async handling failed: " + e.getMessage());
            }
        }, config.getAsyncExecutor());
    }

}
