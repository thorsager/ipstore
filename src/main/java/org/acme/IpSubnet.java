package org.acme;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ip_subnets")
public class IpSubnet implements Serializable {

    /**
     * Will expand byte[] to the left (keeping lower bits lowest)
     * @param length byte size of the returned array
     * @param arr array holding value
     * @return new byte[] of the requested size, containing value of the passed array.
     */
    public static byte[] expandLeft(final int length, final byte[] arr) {
        final var t = new byte[length];
        System.arraycopy(arr,0,t,t.length-arr.length,arr.length);
        return t;
    }
    public static byte[] shrinkLeft(final int length, final byte[] arr) {
        final var first = arr.length-length;
        return Arrays.copyOfRange(arr,first,first+length);
    }

    public IpSubnet(final String ipString) throws AddressStringException {
        final var a = new IPAddressString(ipString);
        a.validate();
        this.cidr = a.toNormalizedString();
        final var  range = a.getSequentialRange();
        if (a.isIPv4()) {
            this.netLength = a.getNetworkPrefixLength() == null ? 32 :a.getNetworkPrefixLength();
            this.lower = expandLeft(16,range.getLower().getBytes());
            this.upper = expandLeft(16,range.getUpper().getBytes());
            this.ipv4 = true;
        } else {
            this.lower = range.getLower().getBytes();
            this.upper = range.getUpper().getBytes();
            this.netLength = a.getNetworkPrefixLength() == null ? 128 :a.getNetworkPrefixLength();
        }
    }

    @Id
    @GeneratedValue
    @Getter
    private Long id;

    @Getter
    private String cidr;
    @Getter
    private Integer netLength;
    @Column(columnDefinition = "BINARY(16) NOT NULL", length = 16)
    private byte[] lower;
    @Column(columnDefinition = "BINARY(16) NOT NULL", length = 16)
    private byte[] upper;

    @Getter
    private boolean ipv4;

    @SneakyThrows
    public InetAddress getLowerBound() {
        return InetAddress.getByAddress(ipv4?shrinkLeft(4,lower):lower);
    }
    @SneakyThrows
    public InetAddress getUpperBound() {
        return InetAddress.getByAddress(ipv4?shrinkLeft(4,upper):upper);
    }

    @Override
    public String toString() {
            return "IpSubnet{" +
                    "id=" + id +
                    ", isIPv4=" + ipv4 +
                    ", cidr=" + cidr +
                    ", netLength=" + netLength +
                    ", lower=" + getLowerBound().getHostAddress() +
                    ", upper=" + getUpperBound().getHostAddress() +
                    '}';
    }
}
