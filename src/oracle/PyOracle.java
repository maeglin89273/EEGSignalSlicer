package oracle;

import net.razorvine.pyro.NameServerProxy;
import net.razorvine.pyro.PyroProxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 8/17/15.
 */
public class PyOracle {
    private static PyOracle ourInstance = new PyOracle();

    private NameServerProxy nameServer;
    private final Map<String, PyroProxy> oracles;

    public static PyOracle getInstance() {
        return ourInstance;
    }

    private PyOracle() {
        this.oracles = new HashMap<>();
        try {
            this.nameServer = NameServerProxy.locateNS(null);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public PyroProxy getOracle(String name) {
        if (!oracles.containsKey(name)) {
            PyroProxy proxy = null;
            try {
                 proxy = new PyroProxy(this.nameServer.lookup(name));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            oracles.put(name, proxy);
            return proxy;
        }

        return oracles.get(name);
    }

    public void shutdown() {
        for (PyroProxy proxy: oracles.values()) {
            proxy.close();
        }

        this.nameServer.close();
    }
}
