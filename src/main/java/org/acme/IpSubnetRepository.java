package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@ApplicationScoped
public class IpSubnetRepository implements PanacheRepository<IpSubnet> {
    public PanacheQuery<IpSubnet> findContaining(final InetAddress address) {
        final byte[] bytes;
        if (address instanceof Inet6Address) {
            bytes = address.getAddress();
        } else {
            bytes = IpSubnet.expandLeft(16,address.getAddress());
        }
        return find("?1 between lower and upper", bytes);
    }
}
