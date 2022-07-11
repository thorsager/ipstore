package org.acme;

import inet.ipaddr.AddressStringException;
import org.acme.model.CreateIPSubnetDTO;

import javax.print.attribute.standard.Media;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

@Path("/")
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class IpSubnetResource {

    private final IpSubnetRepository repo;

    public IpSubnetResource(final IpSubnetRepository repo) {
        this.repo = repo;
    }

    @GET
    public List<IpSubnet> get(final @QueryParam("ipAddress") String address) {
        if (null == address) {
            return repo.findAll().stream().collect(Collectors.toList());
        } else {
            try {
                final var iAdr = InetAddress.getByName(address);
                return repo.findContaining(iAdr).stream().collect(Collectors.toList());
            } catch (final UnknownHostException e) {
                throw new ClientErrorException(400,e);
            }
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(final CreateIPSubnetDTO csn, final @Context UriInfo uriInfo) {
        try {
            final var sn = new IpSubnet(csn.getCidr());
            repo.persist(sn);
            return Response.created(uriInfo.getRequestUri().resolve(sn.getId()+"")).entity(sn).build();
        } catch (final AddressStringException e) {
            throw new ClientErrorException(400,e);
        }
    }

    @GET
    @Path("/{id}")
    public IpSubnet getSubnet(final @PathParam("id") Long id) {
        final var sn = repo.findById(id);
        if (null == sn) {
            throw new NotFoundException();
        }
        return sn;
    }

    @DELETE
    @Path("/{id}")
    public void deleteSubnet(final @PathParam("id") Long id) {
        if (!repo.deleteById(id)) {
            throw new NotFoundException();
        }
    }
}