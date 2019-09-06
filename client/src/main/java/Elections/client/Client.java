package Elections.client;

import Elections.AdministrationService;
import Elections.Exceptions.ElectionStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    private static Logger logger = LoggerFactory.getLogger(Client.class);
    private static final int port = 8090;

    public static void main(String[] args) throws RemoteException, NotBoundException {
        logger.info("Elections Client Starting ...");
        System.out.println("Elections Client Starting ...");

        final Registry registry = LocateRegistry.getRegistry("192.168.0.246", port); // cliente
        final AdministrationService as = (AdministrationService) registry.lookup("administration_service");
        as.openElections();
        try {
            System.out.println(as.getElectionState());
        } catch (ElectionStateException e) {
            e.printStackTrace();
        }
        //final Service handle = (AdministrationService) Naming.lookup("//xx.xx.xx.xx:1099/service");
    }
}
