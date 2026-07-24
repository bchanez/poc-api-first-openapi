package com.poc.api.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.poc.api.model.Genre;
import com.poc.api.model.Livre;

/**
 * Petit magasin en mémoire pour le POC (aucune persistance, aucun Spring).
 * Remplaçable par un vrai repository sans toucher au controller ni au contrat.
 */
final class CatalogueStore {

    static final CatalogueStore INSTANCE = new CatalogueStore();

    private final ConcurrentHashMap<UUID, Livre> parId = new ConcurrentHashMap<>();

    private CatalogueStore() {
        ajouterJeuDeDonnees();
    }

    List<Livre> tout() {
        return List.copyOf(parId.values());
    }

    Optional<Livre> parId(UUID id) {
        return Optional.ofNullable(parId.get(id));
    }

    Livre enregistrer(Livre livre) {
        parId.put(livre.getId(), livre);
        return livre;
    }

    boolean supprimer(UUID id) {
        return parId.remove(id) != null;
    }

    private void ajouterJeuDeDonnees() {
        enregistrer(seed(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                "L'Étranger", "Albert Camus", "978-2-07-036002-4",
                Genre.ROMAN, 159, 7.40, true, LocalDate.of(1942, 6, 15)));
        enregistrer(seed(UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7"),
                "Le Petit Prince", "Antoine de Saint-Exupéry", "978-2-07-040850-4",
                Genre.JEUNESSE, 96, 6.90, false, LocalDate.of(1943, 4, 6)));
    }

    private static Livre seed(UUID id, String titre, String auteur, String isbn,
                              Genre genre, int pages, double prix, boolean dispo, LocalDate parution) {
        Livre l = new Livre();
        l.setId(id);
        l.setTitre(titre);
        l.setAuteur(auteur);
        l.setIsbn(isbn);
        l.setGenre(genre);
        l.setNombrePages(pages);
        l.setPrix(prix);
        l.setDisponible(dispo);
        l.setDateParution(parution);
        return l;
    }
}
