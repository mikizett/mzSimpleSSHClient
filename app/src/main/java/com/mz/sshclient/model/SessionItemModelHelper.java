package com.mz.sshclient.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;

public final class SessionItemModelHelper {

    private static final Logger LOG = LogManager.getLogger(SessionItemModelHelper.class);

    public static SessionItemModel convertToSessionItemModel(final SessionItemDraftModel draftModel) {
        final Generic<SessionItemModel, SessionItemDraftModel> deepCopyFromDraftModel = new Generic<>(SessionItemModel.class, draftModel);
        return deepCopy(deepCopyFromDraftModel);
    }

    public static SessionItemDraftModel convertToSessionItemDraftModel(final SessionItemModel model) {
        final Generic<SessionItemDraftModel, SessionItemModel> deepCopyFromModel = new Generic<>(SessionItemDraftModel.class, model);
        return deepCopy(deepCopyFromModel);
    }

    private static <T extends SessionItemModel, E extends SessionItemModel> T deepCopy(Generic<T, E> that) {
        try {
            final T t = that.buildOne();
            final E e = that.getObject();

            t.setId(new String(e.id));
            t.setName(new String(e.name));
            t.setHost(new String(e.getHost()));
            t.setPort(new String(e.getPort()));
            t.setUser(new String(e.getUser()));
            t.setPrivateKeyFile(new String(e.getPrivateKeyFile()));
            t.setLocalFolder(new String(e.getLocalFolder()));
            t.setRemoteFolder(new String(e.getRemoteFolder()));
            t.setJumpHost(new String(e.getJumpHost()));

            return t;

        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    private static class Generic<T extends SessionItemModel, E extends SessionItemModel> {
        private final Class<T> clazz;
        private final E e;

        public Generic(Class<T> clazz, E e) {
            this.clazz = clazz;
            this.e = e;
        }

        public T buildOne() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            return clazz.getDeclaredConstructor().newInstance();
        }

        public E getObject() {
            return e;
        }
    };
}
