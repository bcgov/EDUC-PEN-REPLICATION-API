package ca.bc.gov.educ.api.pen.replication.rest;

import ca.bc.gov.educ.api.pen.replication.properties.ApplicationProperties;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

/**
 * The type Rest web client.
 */
@Configuration
@Profile("!test")
public class RestWebClient {

  /**
   * The Props.
   */
  private final ApplicationProperties props;

  /**
   * Instantiates a new Rest web client.
   *
   * @param props the props
   */
  public RestWebClient(ApplicationProperties props) {
    this.props = props;
  }

  /**
   * Web client web client.
   *
   * @param builder the builder
   * @return the web client
   */
  @Bean
  @Autowired
  WebClient webClient(final WebClient.Builder builder) {
    val clientRegistryRepo = new InMemoryReactiveClientRegistrationRepository(ClientRegistration
        .withRegistrationId(props.getClientID())
        .tokenUri(props.getTokenURL())
        .clientId(props.getClientID())
        .clientSecret(props.getClientSecret())
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .build());
    val clientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistryRepo);
    val authorizedClientManager =
        new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistryRepo, clientService);
    val oauthFilter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
    oauthFilter.setDefaultClientRegistrationId(props.getClientID());
    val factory = new DefaultUriBuilderFactory();
    factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
    return builder
        .uriBuilderFactory(factory)
        .filter(oauthFilter)
        .build();
  }
}
