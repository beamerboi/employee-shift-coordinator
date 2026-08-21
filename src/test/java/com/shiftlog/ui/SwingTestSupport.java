package com.shiftlog.ui;

import static org.assertj.core.api.Assertions.fail;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;

final class SwingTestSupport {

    private SwingTestSupport() {
    }

    static void onEdt(Runnable action) {
        onEdt(() -> {
            action.run();
            return null;
        });
    }

    static <T> T onEdt(Callable<T> action) {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    result.set(action.call());
                } catch (Throwable throwable) {
                    failure.set(throwable);
                }
            });
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        } catch (InvocationTargetException exception) {
            throw new AssertionError(exception.getCause());
        }
        if (failure.get() != null) {
            fail("Swing action failed", failure.get());
        }
        return result.get();
    }

    static <T extends Component> T component(Container root, String name, Class<T> type) {
        if (name.equals(root.getName()) && type.isInstance(root)) {
            return type.cast(root);
        }
        for (Component child : root.getComponents()) {
            if (name.equals(child.getName()) && type.isInstance(child)) {
                return type.cast(child);
            }
            if (child instanceof Container container) {
                T found = find(container, name, type);
                if (found != null) {
                    return found;
                }
            }
        }
        throw new AssertionError("Component not found: " + name);
    }

    private static <T extends Component> T find(Container root, String name, Class<T> type) {
        for (Component child : root.getComponents()) {
            if (name.equals(child.getName()) && type.isInstance(child)) {
                return type.cast(child);
            }
            if (child instanceof Container container) {
                T found = find(container, name, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
