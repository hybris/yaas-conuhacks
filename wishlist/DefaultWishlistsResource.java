
package com.yaas.conuhack.api.generated;

import javax.inject.Singleton;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import com.yaas.conuhack.RepositoryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
* Resource class containing the custom logic. Please put your logic here!
*/
@Component("apiWishlistsResource")
@Singleton
public class DefaultWishlistsResource implements com.yaas.conuhack.api.generated.WishlistsResource
{
	@javax.ws.rs.core.Context
	private javax.ws.rs.core.UriInfo uriInfo;

	@Autowired
	private RepositoryClient repositoryClient;

	@Override
	public Response get(final YaasAwareParameters yaasAware)
	{
		final List<Wishlist> wishlistList = repositoryClient.getAll(yaasAware, new GenericType<List<Wishlist>>(){});
		return Response.ok().entity(wishlistList).build();
	}

	@Override
	public Response post(final YaasAwareParameters yaasAware, final Wishlist wishlist)
	{
		final Response postResponse = repositoryClient.post(yaasAware, wishlist);
		return Response.created(postResponse.getLocation()).build();
	}

	@Override
	public Response getByWishlistId(final YaasAwareParameters yaasAware, final java.lang.String wishlistId)
	{
		final Wishlist device = repositoryClient.get(yaasAware, wishlistId, Wishlist.class);
		return Response.ok().entity(device).build();
	}

	@Override
	public Response putByWishlistId(final YaasAwareParameters yaasAware, final java.lang.String wishlistId, final Wishlist wishlist)
	{
		return Response.ok().build();
	}

	@Override
	public Response deleteByWishlistId(final YaasAwareParameters yaasAware, final java.lang.String wishlistId)
	{
		return repositoryClient.delete(yaasAware, wishlistId);
	}
}
