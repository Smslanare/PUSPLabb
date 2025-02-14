package etsf20.basesystem.web.pages;

import io.javalin.http.Context;
import jakarta.servlet.http.HttpSession;
import etsf20.basesystem.domain.models.UserRole;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

public class Session {
    private final Context ctx;
    private String user;
    private String displayName;
    private UserRole userRole;
    private final HashMap<UUID,StateValue> states;

    private final static int MAX_STATE_ENTRIES = 32;

    private static class StateValue implements Serializable {
        private final Instant refreshed;
        private final Serializable value;

        public StateValue(Serializable value) {
            Objects.requireNonNull(value);
            this.refreshed = Instant.now();
            this.value = value;
        }

        public Instant getRefreshed() {
            return refreshed;
        }

        public Object getValue() {
            return value;
        }
    }
    
    private Session(Context ctx, String user, String displayName, UserRole userRole, HashMap<UUID,StateValue> states) {
        this.ctx = ctx;
        this.user = user;
        this.displayName = displayName;
        this.userRole = userRole;
        this.states = states;
    }

    public boolean isLoggedIn() {
        return !user.isEmpty();
    }

    public String username() {
        return this.user;
    }

    public String displayName() {
        return this.displayName;
    }

    public UserRole userRole() {
        return this.userRole;
    }

    public void login(String username, String displayname, UserRole userRole) {
        HttpSession session = ctx.req().getSession(false);
        if(session != null) {
            ctx.req().changeSessionId();
        }

        ctx.sessionAttribute("user.name", username);
        ctx.sessionAttribute("user.role", userRole);
        ctx.sessionAttribute("user.displayname", displayname);
        this.user = username;
        this.displayName = displayname;
        this.userRole = userRole;
    }

    public void logout() {
        HttpSession session = ctx.req().getSession(false);
        if(session != null) {
            session.invalidate();
        }
    }

    /**
     * Get state value
     *
     * <p><b>Remarks:</b> Deserialization failure is unfortunately silent and can happen if state type has changed.
     * In case of errors such as above a default value will be used.</p>
     *
     * @param stateRef   state reference, use {@code Page.stateReference()} to get it
     * @param type  expected type of value in storage, required to validate type due to java type-erasure
     * @param defaultValue supplier for a default value
     * @return stored value or default value
     * @param <T> state type that must be serializable
     */
    public <T extends Serializable> T getOrDefault(UUID stateRef, Class<T> type, Supplier<T> defaultValue) {
        StateValue stateValue = states.get(stateRef);
        if(stateValue == null) {
            stateValue = new StateValue(defaultValue.get());
            states.put(stateRef, stateValue);
            ctx.sessionAttribute("state", states);

            @SuppressWarnings("unchecked")
            T value = (T)stateValue.getValue();
            return value;
        }

        if(!type.isAssignableFrom(stateValue.getValue().getClass())) {
            stateValue = new StateValue(defaultValue.get());
            states.put(stateRef, stateValue);
            ctx.sessionAttribute("state", states);
        }

        @SuppressWarnings("unchecked")
        T value = (T)stateValue.getValue();
        return value;
    }

    /**
     * Remove state values for this state reference
     * @param stateRef state reference
     */
    public void invalidate(UUID stateRef) {
        this.states.remove(stateRef);
        ctx.sessionAttribute("state", states); // update session data
    }

    /**
     * Update state value
     *
     * <p><b>Known issue:</b> Serialization failure is unfortunately silent inside Javalin.</p>
     * @param stateRef  state reference
     * @param value value to save
     */
    public void put(UUID stateRef, Serializable value) {
        // states is referenced by Javalin and will be saved to database at the end of the request.
        states.put(stateRef, new StateValue(value));
        ctx.sessionAttribute("state", states); // update session data
    }

    /**
     * Create session from context
     * @param ctx context
     * @return session valid for this request
     */
    public static Session from(Context ctx) {
        String displayname = ctx.sessionAttribute("user.displayname");
        String username = ctx.sessionAttribute("user.name");
        UserRole userRole = ctx.sessionAttribute("user.role");
        if(username == null) {
            username = "";
        }

        if(displayname == null) {
            displayname = "";
        }

        if(userRole == null) {
            userRole = UserRole.GUEST;
        }

        HashMap<UUID,StateValue> states = ctx.sessionAttribute("state");
        if(states != null) {
            final Instant now = Instant.now();
            if(!states.isEmpty() && states.size() <= MAX_STATE_ENTRIES) {
                // remove old ones
                states.entrySet()
                      .stream()
                      .filter(e ->  (now.getEpochSecond() - e.getValue().getRefreshed().getEpochSecond()) >= 20*60)
                      .map(Map.Entry::getKey)
                      .toList()
                      .forEach(states::remove);
            } else {
                // retain the newest MAX_STATE_ENTRIES
                HashMap<UUID,StateValue> retain = new HashMap<>();

                states.entrySet()
                      .stream()
                      .sorted(Comparator.comparing((Map.Entry<UUID,StateValue> x) -> x.getValue().getRefreshed()).reversed())
                      .limit(MAX_STATE_ENTRIES)
                      .forEach(e -> retain.put(e.getKey(), e.getValue()));

                states = retain;
            }

        } else {
            states = new HashMap<>();
        }
        ctx.sessionAttribute("state", states);

        return new Session(ctx, username, displayname, userRole, states);
    }

}

