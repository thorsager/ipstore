package org.acme;

import inet.ipaddr.AddressStringException;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@Transactional
public class IpSubnetRepositoryTest {

    @Inject
    IpSubnetRepository repo;

    @BeforeEach
    void before() throws AddressStringException {
        repo.deleteAll();
        repo.persist(new IpSubnet("192.168.100.0/24"));
        repo.persist(new IpSubnet("10.0.0.0/8"));
        repo.persist(new IpSubnet("172.16.0.0/12"));
        repo.persist(new IpSubnet("2001:db8:1:fa00::/56"));
        repo.persist(new IpSubnet("2001:db8:1:ff00::/56"));
        repo.persist(new IpSubnet("2001:db8:1:fe00::/56"));
        repo.flush();
    }

    @Test
    void creatingSubnet()  {
        final var actual = repo.findAll().stream().collect(Collectors.toList());
        assertEquals(6,actual.size());
    }

    @Test
    void findContaining_v6() throws UnknownHostException {
        final var a = InetAddress.getByName("2001:db8:1:ff00::1");
        final var actual = repo.findContaining(a).stream().collect(Collectors.toList());
        assertEquals(1,actual.size());
        System.out.println(actual.get(0));
        assertEquals("2001:db8:1:ff00:0:0:0:0/56",actual.get(0).getCidr());
    }

    @Test
    void findContaining_notFound_v6() throws UnknownHostException {
        final var a = InetAddress.getByName("2001:db8:1:ef00::1");
        final var actual = repo.findContaining(a).stream().collect(Collectors.toList());
        assertEquals(0,actual.size());
    }

    @Test
    void findContaining_v4() throws UnknownHostException {
        final var a = InetAddress.getByName("172.16.8.2");
        final var actual = repo.findContaining(a).stream().collect(Collectors.toList());
        assertEquals(1,actual.size());
        System.out.println(actual.get(0));
        assertEquals("172.16.0.0/12",actual.get(0).getCidr());

    }

    @Test
    void findContaining_notFound_v4() throws UnknownHostException {
        final var a = InetAddress.getByName("192.168.1.1");
        final var actual = repo.findContaining(a).stream().collect(Collectors.toList());
        System.out.println(actual);
        assertEquals(0,actual.size());
    }
}
