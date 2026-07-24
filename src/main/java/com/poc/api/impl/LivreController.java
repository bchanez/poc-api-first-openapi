package com.poc.api.impl;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.poc.api.LivresApi;
import com.poc.api.model.Erreur;
import com.poc.api.model.Genre;
import com.poc.api.model.Livre;
import com.poc.api.model.NouveauLivre;
import com.poc.api.model.PageLivres;

import jakarta.ws.rs.core.Response;

/**
 * Implémentation du contrat {@link LivresApi} généré depuis openapi.yaml.
 *
 * <p>100 % Jakarta EE : le seul héritage est l'interface JAX-RS générée. Aucun
 * import Spring, aucune annotation Swagger ici — la doc vit dans openapi.yaml.
 *
 * <p>Variante {@code returnResponse=true} : les méthodes renvoient une
 * {@link Response}, ce qui permet de piloter finement le code de statut
 * (201, 204…) et les en-têtes (Location) comme déclaré dans le contrat.
 */
public class LivreController implements LivresApi {

    private final CatalogueStore store = CatalogueStore.INSTANCE;

    @Override
    public Response listerLivres(Genre genre, Boolean disponible, String recherche,
                                 Integer page, Integer taille, String tri) {
        Stream<Livre> flux = store.tout().stream();

        if (genre != null) {
            flux = flux.filter(l -> genre.equals(l.getGenre()));
        }
        if (disponible != null) {
            flux = flux.filter(l -> disponible.equals(l.getDisponible()));
        }
        if (recherche != null && !recherche.isBlank()) {
            String q = recherche.toLowerCase();
            flux = flux.filter(l -> contient(l.getTitre(), q) || contient(l.getAuteur(), q));
        }

        List<Livre> filtres = flux.sorted(comparateur(tri)).toList();

        int from = Math.min(page * taille, filtres.size());
        int to = Math.min(from + taille, filtres.size());

        PageLivres reponse = new PageLivres();
        reponse.setTotal((long) filtres.size());
        reponse.setPage(page);
        reponse.setTaille(taille);
        reponse.setElements(filtres.subList(from, to));
        return Response.ok(reponse).build();
    }

    @Override
    public Response obtenirLivre(UUID id) {
        return store.parId(id)
                .map(livre -> Response.ok(livre).build())
                .orElseGet(() -> introuvable(id));
    }

    @Override
    public Response creerLivre(NouveauLivre nouveau) {
        Livre livre = new Livre();
        livre.setId(UUID.randomUUID());
        livre.setTitre(nouveau.getTitre());
        livre.setAuteur(nouveau.getAuteur());
        livre.setIsbn(nouveau.getIsbn());
        livre.setGenre(nouveau.getGenre());
        livre.setNombrePages(nouveau.getNombrePages());
        livre.setPrix(nouveau.getPrix());
        livre.setDisponible(nouveau.getDisponible());
        livre.setDateParution(nouveau.getDateParution());
        store.enregistrer(livre);

        // 201 + Location, exactement comme déclaré dans openapi.yaml
        return Response.created(URI.create("/api/livres/" + livre.getId()))
                .entity(livre)
                .build();
    }

    @Override
    public Response supprimerLivre(UUID id) {
        if (store.supprimer(id)) {
            return Response.noContent().build();   // 204
        }
        return introuvable(id);
    }

    // --- helpers ---

    private static boolean contient(String valeur, String q) {
        return valeur != null && valeur.toLowerCase().contains(q);
    }

    private static Comparator<Livre> comparateur(String tri) {
        return switch (tri == null ? "titre" : tri) {
            case "auteur" -> Comparator.comparing(Livre::getAuteur, nullsafe());
            case "dateParution" -> Comparator.comparing(Livre::getDateParution,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "prix" -> Comparator.comparing(Livre::getPrix,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Livre::getTitre, nullsafe());
        };
    }

    private static Comparator<String> nullsafe() {
        return Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);
    }

    /** 404 avec le corps d'erreur standardisé décrit dans le contrat (schéma Erreur). */
    private static Response introuvable(UUID id) {
        Erreur erreur = new Erreur();
        erreur.setCode(Erreur.CodeEnum.NON_TROUVE);
        erreur.setMessage("Aucun livre pour l'identifiant " + id + ".");
        return Response.status(Response.Status.NOT_FOUND).entity(erreur).build();
    }
}
