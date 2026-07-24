package com.poc.api.impl;

import java.util.stream.Collectors;

import com.poc.api.model.Erreur;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Transforme les violations de Bean Validation (contraintes @NotNull/@Size/@Pattern
 * générées sur les DTO depuis openapi.yaml) en réponse 400 conforme au schéma
 * {@code Erreur} du contrat.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException ex) {
        String detail = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(" ; "));

        Erreur erreur = new Erreur();
        erreur.setCode(Erreur.CodeEnum.REQUETE_INVALIDE);
        erreur.setMessage(detail.isBlank() ? "Requête invalide." : detail);

        return Response.status(Response.Status.BAD_REQUEST).entity(erreur).build();
    }
}
