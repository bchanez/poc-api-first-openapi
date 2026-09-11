package com.poc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Harnais E2E canonique : boote TOUTE l'application sur un port aléatoire
 * ({@code RANDOM_PORT}) et expose un {@link TestRestTemplate} pour taper de VRAIS
 * appels HTTP, exactement comme un client réel.
 *
 * <p>On traverse ainsi le vrai servlet Jersey (monté sur {@code /api/*}) et Spring
 * Security. MockMvc ne conviendrait PAS ici : il ne connaît que le
 * {@code DispatcherServlet} Spring et raterait complètement la couche JAX-RS.
 *
 * <p>Les annotations {@code @SpringBootTest} / {@code @AutoConfigureTestRestTemplate}
 * portées ici sont héritées par toutes les sous-classes (Spring les résout sur la
 * hiérarchie), à la manière de {@code AbstractE2EIT} dans batchmeal.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public abstract class AbstractE2EIT {

  @Autowired private TestRestTemplate rest;

  /** Client authentifié en {@code admin/admin} — l'API réclame une auth basic. */
  protected TestRestTemplate api() {
    return rest.withBasicAuth("admin", "admin");
  }

  /** Client anonyme — pour vérifier les rejets d'authentification (401). */
  protected TestRestTemplate anonymous() {
    return rest;
  }
}
