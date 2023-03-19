package com.mz.sshclient.services;

import com.mz.sshclient.services.exceptions.ServiceRegistrationException;
import com.mz.sshclient.services.impl.PasswordStorageService;
import com.mz.sshclient.services.impl.SshConnectionService;
import com.mz.sshclient.services.impl.SessionDataService;
import com.mz.sshclient.services.interfaces.IPasswordStorageService;
import com.mz.sshclient.services.interfaces.ISshConnectionObservableService;
import com.mz.sshclient.services.interfaces.IService;
import com.mz.sshclient.services.interfaces.ISessionDataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ServiceRegistration {

    private static final Logger LOG = LogManager.getLogger(ServiceRegistration.class);

    public static void registration() throws ServiceRegistrationException {
        try {
            register(IPasswordStorageService.class, new PasswordStorageService());
            register(ISessionDataService.class, new SessionDataService());
            register(ISshConnectionObservableService.class, new SshConnectionService());
        } catch (Exception e) {
            throw new ServiceRegistrationException("Could not register service", e);
        }
    }

    private static <T extends IService, E extends T> void register(Class<? extends IService> clazz, E objectReference) {
        try {
            ServiceRegistry.registerServices(clazz, objectReference);
        } catch (ServiceRegistrationException e) {
            LOG.error("Could not register service " + clazz);
        }
    }
}
