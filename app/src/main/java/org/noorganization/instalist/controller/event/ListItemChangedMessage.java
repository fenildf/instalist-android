package org.noorganization.instalist.controller.event;

import org.noorganization.instalist.model.ListEntry;

/**
 * Event class for notification about changes in a specific list.
 * Created by daMihe on 17.07.2015.
 */
public class ListItemChangedMessage {
    public enum Change {
        CREATED,
        CHANGED,
        DELETED
    }

    public Change    mChange;
    public ListEntry mEntry;

    public ListItemChangedMessage(Change _change, ListEntry _entry) {
        mChange = _change;
        mEntry  = _entry;
    }
}