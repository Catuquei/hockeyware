package org.hockey.hockeyware.client.manager;

import org.hockey.hockeyware.client.HockeyWare;
import org.hockey.hockeyware.client.util.Globals;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author linustouchtips
 * @since 06/08/2021
 */
public class ThreadManager {

    // thread used by all modules
    private final ClientService clientService = new ClientService();

    // processes
    private static final Queue<Runnable> clientProcesses = new ArrayDeque<>();

    public ThreadManager() {

        // start the client thread
        clientService.setName("hockeyware-client-thread");
        clientService.setDaemon(true);
        clientService.start();
    }

    public static class ClientService extends Thread implements Globals {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // check if the mc world is running
                    if (fullNullCheck()) {

                        // run and remove the latest service
                        if (!clientProcesses.isEmpty()) {
                            clientProcesses.poll().run();
                        }

                        HockeyWare.INSTANCE.moduleManager.getModules().forEach(module -> {
                            try {
                                if (module.isOn()) {
                                    module.onThread();
                                }
                            } catch (Exception exception) {

                                // print stacktrace if in dev environment
                                // don't mind if i do
                                exception.printStackTrace();
                            }
                        });
                    }

                    // give up thread resources
                    else {
                        Thread.yield();
                    }

                } catch(Exception exception) {

                    // print stacktrace if in dev environment
                    // don't mind if i do
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     * Submit a new runnable to the thread
     * @param in The runnable
     */
    public void submit(Runnable in) {
        clientProcesses.add(in);
    }

    /**
     * Gets the main client thread
     * @return The main client thread
     */
    public ClientService getService() {
        return clientService;
    }
}
