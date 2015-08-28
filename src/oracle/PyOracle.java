package oracle;

import net.razorvine.pickle.PickleException;
import net.razorvine.pyro.NameServerProxy;
import net.razorvine.pyro.PyroException;
import net.razorvine.pyro.PyroProxy;
import net.razorvine.pyro.PyroURI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 8/17/15.
 */
public class PyOracle {
    private static PyOracle ourInstance = new PyOracle();

    private NameServerProxy nameServer;

    public static PyOracle getInstance() {
        return ourInstance;
    }

    private PyOracle() {
        try {
            this.nameServer = NameServerProxy.locateNS(null);
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println(e);
            System.out.println("pyro name server is not found");
        }
    }

    public PyroProxy getOracle(String name) {
        name = "oracle." + name;

        PyroProxy proxy = null;
        try {
             proxy = new ReconnectingPyroProxy(name);
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("oracle " + name + " is not found");
            return null;
        }
        return proxy;

    }



    public void shutdown() {
        this.nameServer.close();
    }


    public class ReconnectingPyroProxy extends PyroProxy {
        private final String name;

        private ReconnectingPyroProxy(String name) throws IOException {
            super(nameServer.lookup(name));
            this.name = name;
        }

        @Override
        public Object call(String method, Object... arguments) throws PickleException, PyroException, IOException {
            try {
                return super.call(method, arguments);
            } catch (IOException e) {
                this.reconnect();
                return super.call(method, arguments);
            }
        }

        @Override
        public void call_oneway(String method, Object... arguments) throws PickleException, PyroException, IOException {
            try {
                super.call_oneway(method, arguments);
            } catch (IOException e) {
                this.reconnect();
                super.call_oneway(method, arguments);
            }

        }

        private void reconnect() throws IOException {
            this.close();
            PyroURI uri = nameServer.lookup(this.name);
            this.hostname = uri.host;
            this.port = uri.port;
            this.objectid = uri.objectid;
        }


    }
}
