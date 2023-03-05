package com.mz.sshclient.services;

import com.mz.sshclient.services.exceptions.ServiceRegistrationException;
import com.mz.sshclient.services.impl.SessionDataService;
import com.mz.sshclient.services.interfaces.IService;
import com.mz.sshclient.services.interfaces.ISessionDataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ServiceRegistration {

    private static final Logger LOG = LogManager.getLogger(ServiceRegistration.class);

    public static void registration() {
        register(ISessionDataService.class, new SessionDataService());
    }

    private static <T extends IService, E extends T> void register(Class<? extends IService> clazz, E objectReference) {
        try {
            ServiceRegistry.registerServices(clazz, objectReference);
        } catch (ServiceRegistrationException e) {
            LOG.error("Could not register service " + clazz);
        }
    }
}
