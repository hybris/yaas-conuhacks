package com.yaas.conuhack;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.base.Preconditions;
import com.sap.cloud.yaas.servicesdk.authorization.AuthorizationScope;
import com.sap.cloud.yaas.servicesdk.authorization.DiagnosticContext;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionTemplate;
import com.yaas.conuhack.api.generated.YaasAwareParameters;
import org.glassfish.jersey.filter.LoggingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.logging.Logger;

@Component
public class RepositoryClient
{
	@Autowired
	private AuthorizedExecutionTemplate authorizedExecutionTemplate;

	@Value("${yaas.proxy.enabled:false}")
	private boolean yaasProxyEnabled = false;

	@Value("${testTenant}")
	private String testTenant;

	@Value("${client}")
	private String client;

	@Value("${REPOSITORY_BASE_URL}")
	private String repositoryBaseUrl;

	@Value("${COLLECTION}")
	private String collection;

	private final Client rsClient;

	public RepositoryClient()
	{
		// custom jackson configuration that allows to ignore unmapped values and does not marshall null values
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		final JacksonJsonProvider jsonProvider =
				new JacksonJaxbJsonProvider(mapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		rsClient = ClientBuilder.newBuilder()
				.register(new LoggingFilter(Logger.getLogger(RepositoryClient.class.getName()), true))
				.register(jsonProvider)
				.build();
	}

	public <T> Response post(final YaasAwareParameters yaasAware, final T document)
	{
		final AuthorizationScope scope = new AuthorizationScope(yaasAware.getHybrisTenant(),
				Arrays.asList("hybris.document_manage"));
		final DiagnosticContext context = new DiagnosticContext("test", 1);
		final Response response = authorizedExecutionTemplate.executeAuthorized(
				scope,context, token -> {
					return getBasePath(yaasAware)
							.request().header("Authorization", "Bearer " + token.getValue())
							.header("hybris-metaData", "startDate:date;endDate:date")
							.buildPost(Entity.entity(document, MediaType.APPLICATION_JSON)).invoke();
				});
		return response;
	}

	public <T> T get(final YaasAwareParameters yaasAware, final String id, final Class<T> clazz)
	{
		final AuthorizationScope scope = new AuthorizationScope(yaasAware.getHybrisTenant(),
				Arrays.asList("hybris.document_view"));
		final DiagnosticContext context = new DiagnosticContext("test", 1);
		final Response response = authorizedExecutionTemplate.executeAuthorized(
				scope,
				context, token -> {
				return getBasePath(yaasAware).path("{dataId}").resolveTemplate("dataId", id).request()
						.header("Authorization", "Bearer " + token.getValue()).buildGet().invoke();
			});
		return response.readEntity(clazz);
	}

	public <T> T getAll(final YaasAwareParameters yaasAware, final GenericType<T> clazz)
	{
		return getAllByQuery(yaasAware, clazz, null, null);
	}

	public <T> T getAllByQuery(final YaasAwareParameters yaasAware,
							   final GenericType<T> clazz,
							   final String query,
							   final String metaData)
	{
		final AuthorizationScope scope = new AuthorizationScope(yaasAware.getHybrisTenant(),
				Arrays.asList("hybris.document_view"));
		final DiagnosticContext context = new DiagnosticContext("test", 1);
		final Response response = authorizedExecutionTemplate.executeAuthorized(
				scope,context, token -> {
					WebTarget base = getBasePath(yaasAware);
						if (query != null) {
							base = base.queryParam("q", query);
						}
						Invocation.Builder request = base.request().header("Authorization", "Bearer " + token.getValue());
						if (metaData != null) {
							request = request.header("hybris-metaData", metaData);
						}
						return request.buildGet().invoke();
				});
		return response.readEntity(clazz);
	}

	public Response delete(final YaasAwareParameters yaasAware, final String id)
	{
		final AuthorizationScope scope = new AuthorizationScope(yaasAware.getHybrisTenant(),
				Arrays.asList("hybris.document_manage"));
		final DiagnosticContext context = new DiagnosticContext("test", 1);
		return authorizedExecutionTemplate.executeAuthorized(scope,context, token -> {
			return getBasePath(yaasAware)
					.path("{dataId}").resolveTemplate("dataId", id)
					.request().header("Authorization", "Bearer " + token.getValue()).buildDelete().invoke();
		});
	}

	public Response deleteAll(final YaasAwareParameters yaasAware)
	{
		final AuthorizationScope scope = new AuthorizationScope(yaasAware.getHybrisTenant(),
				Arrays.asList("hybris.document_manage"));
		final DiagnosticContext context = new DiagnosticContext("test", 1);
		return authorizedExecutionTemplate.executeAuthorized(scope,context, token -> {
			final WebTarget base = getBasePath(yaasAware);
			final Invocation.Builder request = base.request().header("Authorization", "Bearer " + token.getValue());
			return request.buildDelete().invoke();
		});
	}

	protected WebTarget getBasePath(final YaasAwareParameters yaasAware)
	{
		String tenant = testTenant;
		if (yaasProxyEnabled)
		{
			Preconditions.checkNotNull(yaasAware, "yaasAware must not be null");
			Preconditions.checkNotNull(yaasAware.getHybrisTenant(), "yaasAware must have a tenant");
			tenant = yaasAware.getHybrisTenant();
		}

		return rsClient.target(repositoryBaseUrl).path("/data/"+collection).resolveTemplate("tenant", tenant).resolveTemplate("client", client);
	}

	protected String getTenantForToken(final YaasAwareParameters yaasAware)
	{
		if (yaasAware == null || !yaasProxyEnabled)
		{
			return null;
		}

		return yaasAware.getHybrisTenant();
	}

	public boolean isYaasProxyEnabled()
	{
		return yaasProxyEnabled;
	}

	public void setYaasProxyEnabled(final boolean yaasProxyEnabled)
	{
		this.yaasProxyEnabled = yaasProxyEnabled;
	}

}
