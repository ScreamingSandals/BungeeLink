package org.screamingsandals.bungeelink.event;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import org.screamingsandals.bungeelink.servers.Server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/*
 * TODO: Refactor
 * TODO: Make an event manager like here: https://github.com/haq/event-manager and make api for it
 *
*/
public class UpdateServerStatusDispatcher {
    private final List<UpdateServerStatusListener> globalListeners = Collections.synchronizedList(new ArrayList<>());
    private final ListMultimap<Server, UpdateServerStatusListener> listeners = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    public void register(Server server, UpdateServerStatusListener handler) {
        listeners.put(server, handler);
    }

    public void unregister(Server server, UpdateServerStatusListener handler) {
        listeners.remove(server, handler);
    }

    public void fire(Server server) {
        var serverListeners = listeners.get(server);
        if (serverListeners != null) {
            serverListeners.forEach(listener -> listener.onUpdate(server));
        }
        globalListeners.forEach(listener -> listener.onUpdate(server));
    }

    public void register(UpdateServerStatusListener handler) {
        if (!globalListeners.contains(handler)) {
            globalListeners.add(handler);
        }
    }

    public void unregister(UpdateServerStatusListener handler) {
        globalListeners.remove(handler);
    }

    public Collection<UpdateServerStatusListener> getListenersByServer(Server server) {
        return listeners.get(server);
    }

    // This is only for checking who is owner of listeners
    private final ListMultimap<String, UpdateServerStatusListener> listenersByTokens = ArrayListMultimap.create();

    public void register(String token, UpdateServerStatusListener handler) {
        listenersByTokens.put(token, handler);
    }

    public void unregister(String token, UpdateServerStatusListener handler) {
        listenersByTokens.remove(token, handler);
    }

    public boolean doesListenerBelongToToken(UpdateServerStatusListener handler, String token) {
        return listenersByTokens.containsEntry(token, handler);
    }
}
