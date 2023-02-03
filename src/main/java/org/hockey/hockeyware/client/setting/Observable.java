package org.hockey.hockeyware.client.setting;

import java.util.LinkedList;
import java.util.List;

/**
 * A generic {@link java.util.Observable}.
 *
 * @param <T> the type of object Observers will be notified with.
 */
public class Observable<T> {
    private final List<Observer<? super T>> observers = new LinkedList<>();

    /**
     * Notifies all observers about a change.
     *
     * @param value the value to call the observers with.
     * @return the given value for convenience.
     */
    public T onChange(T value) {
        for (Observer<? super T> observer : observers) {
            observer.onChange(value);
        }

        return value;
    }

    /**
     * Adds an Observer if it's not yet added.
     *
     * @param observer the observer to add.
     */
    public void addObserver(Observer<? super T> observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Removes the given observer.
     *
     * @param observer the observer to remove.
     */
    public void removeObserver(Observer<? super T> observer) {
        observers.remove(observer);
    }

}