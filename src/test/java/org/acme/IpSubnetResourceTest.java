package org.acme;

import inet.ipaddr.AddressStringException;
import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.MediaType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class IpSubnetResourceTest {

    @Inject
    IpSubnetRepository repo;

    IpSubnet firstV4 ;
    IpSubnet firstV6 ;

    @BeforeEach
    @Transactional
    void before() throws AddressStringException {
        repo.deleteAll();

        firstV4 = new IpSubnet("192.168.100.0/24");
        repo.persist(firstV4);
        repo.persist(new IpSubnet("10.0.0.0/8"));
        repo.persist(new IpSubnet("172.16.0.0/12"));

        firstV6 =new IpSubnet("2001:db8:1:fa00::/56");
        repo.persist(firstV6);
        repo.persist(new IpSubnet("2001:db8:1:ff00::/56"));
        repo.persist(new IpSubnet("2001:db8:1:fe00::/56"));
        repo.flush();
    }

    @Test
    public void create_v4() {
        final var bc = repo.count();

        given()
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .body("{ \"cidr\": \"192.168.20.0/24\" }")
          .when().post("/")
          .then()
             .statusCode(201)
                .header("Location", CoreMatchers.startsWith("http://localhost"))
             .body("cidr",is("192.168.20.0/24"))
             .body("netLength",is(24))
                .body("ipv4",is(true))
                .body("lowerBound",is("192.168.20.0"))
                .body("upperBound",is("192.168.20.255"));

        Assertions.assertEquals(bc+1,repo.count());
    }

    @Test
    public void create_v6() {
        final var bc = repo.count();

        given()
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .body("{ \"cidr\": \"2001:db8:1:f100::/56\" }")
                .when().post("/")
                .then()
                .statusCode(201)
                .header("Location", CoreMatchers.startsWith("http://localhost"))
                .body("cidr",is("2001:db8:1:f100:0:0:0:0/56"))
                .body("netLength",is(56))
                .body("ipv4",is(false))
                .body("lowerBound",is("2001:db8:1:f100:0:0:0:0"))
                .body("upperBound",is("2001:db8:1:f1ff:ffff:ffff:ffff:ffff"));

        Assertions.assertEquals(bc+1,repo.count());
    }

    @Test
    public void get() {
        given()
                .when().get("/{id}",firstV4.getId())
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("cidr",is(firstV4.getCidr()));
    }

    @Test
    public void get_v6() {
        given()
                .when().get("/{id}",firstV6.getId())
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("cidr",is(firstV6.getCidr()));
    }


    @Test
    public void search_v4_notFound() {
        given()
                .when().get("/?ipAddress={addr}","192.168.202.10")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("", Matchers.hasSize(0));
    }

    @Test
    public void search_v4_found() {
        given()
                .when().get("/?ipAddress={addr}","10.10.0.1")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("", Matchers.hasSize(1))
                .body("[0].cidr", is("10.0.0.0/8"));
    }

    @Test
    public void search_v6_notFound() {
        given()
                .when().get("/?ipAddress={addr}","2001:db8:aa00::1")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("", Matchers.hasSize(0));
    }

    @Test
    public void search_v6_found() {
        given()
                .when().get("/?ipAddress={addr}","2001:db8:1:fe00::1")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("", Matchers.hasSize(1))
                .body("[0].cidr", is("2001:db8:1:fe00:0:0:0:0/56"));
    }

    @Test
    public void delete() {
        final var bc = repo.count();
        given()
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().delete("/{id}",firstV6.getId())
                .then()
                .log().ifValidationFails()
                .statusCode(204);
        Assertions.assertEquals(bc-1,repo.count());
    }

}