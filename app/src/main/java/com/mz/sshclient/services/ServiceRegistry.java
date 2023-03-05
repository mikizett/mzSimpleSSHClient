package com.mz.sshclient.services;

import com.mz.sshclient.services.exceptions.ServiceRegistrationException;
import com.mz.sshclient.services.interfaces.IService;

import java.util.HashMap;
import java.util.Map;

public final class ServiceRegistry {

    private static final Map<Class<? extends IService>, IService> services = new HashMap<>(0);

    public static <T extends IService, E extends T> void registerServices(Class<? extends IService> serviceName, E objectReference) throws ServiceRegistrationException {
        if (containsService(serviceName)) {
            throw new ServiceRegistrationException("Service <" + serviceName.getName() + "> already registered!");
        }
        services.put(serviceName, objectReference);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        return (T) services.get(clazz);
    }

    private static boolean containsService(Class<? extends IService> clazz) {
        return services.keySet().contains(clazz);
    }

}