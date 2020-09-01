package com.hivemq.testcontainer.core;

import org.jetbrains.annotations.NotNull;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.utility.LogUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

class MultiLogMessageWaitStrategy extends AbstractWaitStrategy {

    private final @NotNull ConcurrentHashMap<String, Boolean> regexes = new ConcurrentHashMap<>();

    @Override
    protected void waitUntilReady() {
        WaitingConsumer waitingConsumer = new WaitingConsumer();
        LogUtils.followOutput(DockerClientFactory.instance().client(), waitStrategyTarget.getContainerId(), waitingConsumer);

        Predicate<OutputFrame> waitPredicate = outputFrame -> {
            if (regexes.isEmpty()) {
                return true;
            }
            regexes.entrySet().forEach(stringBooleanEntry -> {
                final boolean matched = outputFrame.getUtf8String().matches("(?s)" + stringBooleanEntry.getKey());
                if (matched) {
                    stringBooleanEntry.setValue(true);
                }
            });
            return regexes.values().stream().reduce(Boolean::logicalAnd).orElse(true);
        };

        try {
            waitingConsumer.waitUntil(waitPredicate, startupTimeout.getSeconds(), TimeUnit.SECONDS, 1);
        } catch (TimeoutException e) {
            throw new ContainerLaunchException("Timed out waiting for log output matching '" + regexes + "'");
        }
    }

    public @NotNull MultiLogMessageWaitStrategy withRegEx(final @NotNull String regEx) {
        regexes.put(regEx, false);
        return this;
    }
}
