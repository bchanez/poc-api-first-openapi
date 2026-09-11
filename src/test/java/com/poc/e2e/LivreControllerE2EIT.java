package com.poc.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.poc.AbstractE2EIT;
import com.poc.api.model.Erreur;
import com.poc.api.model.Genre;
import com.poc.api.model.Livre;
import com.poc.api.model.NouveauLivre;
import com.poc.api.model.PageLivres;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Tests E2E du {@link com.poc.api.impl.LivreController} sur l'API {@code /api/livres},
 * via de vrais appels HTTP (voir {@link AbstractE2EIT}).
 */
class LivreControllerE2EIT extends AbstractE2EIT {

  /** Livre pré-chargé par CatalogueStore (jeu de données du POC). */
  private static final UUID ID_ETRANGER = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");

  @Test
  void should_return_the_seeded_catalogue_when_listing_books() {
    // given — le catalogue pré-chargé du POC

    // when
    ResponseEntity<PageLivres> reponse = api().getForEntity("/api/livres", PageLivres.class);

    // then
    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(reponse.getBody()).isNotNull();
    assertThat(reponse.getBody().getTotal()).isGreaterThanOrEqualTo(2);
    assertThat(reponse.getBody().getElements())
        .extracting(Livre::getTitre)
        .contains("L'Étranger", "Le Petit Prince");
  }

  @Test
  void should_return_the_book_when_it_exists() {
    // given — un identifiant présent dans le catalogue

    // when
    ResponseEntity<Livre> reponse = api().getForEntity("/api/livres/{id}", Livre.class, ID_ETRANGER);

    // then
    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(reponse.getBody()).isNotNull();
    assertThat(reponse.getBody().getId()).isEqualTo(ID_ETRANGER);
    assertThat(reponse.getBody().getTitre()).isEqualTo("L'Étranger");
    assertThat(reponse.getBody().getGenre()).isEqualTo(Genre.ROMAN);
  }

  @Test
  void should_return_404_with_error_body_when_the_book_is_unknown() {
    // given — un identifiant qui n'existe pas
    UUID inconnu = UUID.randomUUID();

    // when
    ResponseEntity<Erreur> reponse = api().getForEntity("/api/livres/{id}", Erreur.class, inconnu);

    // then
    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(reponse.getBody()).isNotNull();
    assertThat(reponse.getBody().getCode()).isEqualTo(Erreur.CodeEnum.NON_TROUVE);
  }

  @Test
  void should_walk_the_full_lifecycle_when_creating_reading_then_deleting_a_book() {
    // given — un nouveau livre valide
    NouveauLivre nouveau = laPeste();

    // when — création
    ResponseEntity<Livre> creation = api().postForEntity("/api/livres", nouveau, Livre.class);

    // then — 201 + Location + corps du livre créé
    assertThat(creation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(creation.getBody()).isNotNull();
    UUID idCree = creation.getBody().getId();
    assertThat(idCree).isNotNull();
    URI location = creation.getHeaders().getLocation();
    assertThat(location).isNotNull();
    assertThat(location.getPath()).endsWith("/api/livres/" + idCree);

    // when — relecture
    ResponseEntity<Livre> relecture = api().getForEntity("/api/livres/{id}", Livre.class, idCree);

    // then — le livre est bien persisté
    assertThat(relecture.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(relecture.getBody()).isNotNull();
    assertThat(relecture.getBody().getTitre()).isEqualTo("La Peste");

    // when — suppression
    ResponseEntity<Void> suppression = supprimer(idCree);

    // then — 204 puis 404 à la relecture
    assertThat(suppression.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    ResponseEntity<Erreur> apres = api().getForEntity("/api/livres/{id}", Erreur.class, idCree);
    assertThat(apres.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void should_return_400_when_the_isbn_is_invalid() {
    // given — un livre dont l'ISBN viole le pattern déclaré dans openapi.yaml
    NouveauLivre invalide = new NouveauLivre();
    invalide.setTitre("Titre valide");
    invalide.setAuteur("Auteur valide");
    invalide.setIsbn("pas-un-isbn");
    invalide.setGenre(Genre.ESSAI);

    // when
    ResponseEntity<String> reponse = api().postForEntity("/api/livres", invalide, String.class);

    // then
    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_return_401_when_calling_the_api_without_authentication() {
    // given — un client anonyme

    // when
    ResponseEntity<String> reponse = anonymous().getForEntity("/api/livres", String.class);

    // then — Spring Security bloque avant d'atteindre Jersey
    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  // --- helpers ---

  private NouveauLivre laPeste() {
    NouveauLivre nouveau = new NouveauLivre();
    nouveau.setTitre("La Peste");
    nouveau.setAuteur("Albert Camus");
    nouveau.setIsbn("978-2-07-036042-0");
    nouveau.setGenre(Genre.ROMAN);
    nouveau.setNombrePages(279);
    nouveau.setPrix(8.60);
    nouveau.setDisponible(true);
    nouveau.setDateParution(LocalDate.of(1947, 6, 10));
    return nouveau;
  }

  private ResponseEntity<Void> supprimer(UUID id) {
    return api().exchange("/api/livres/{id}", HttpMethod.DELETE, null, Void.class, id);
  }
}
