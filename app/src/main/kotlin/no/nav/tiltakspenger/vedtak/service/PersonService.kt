package no.nav.tiltakspenger.vedtak.service

import no.nav.tiltakspenger.vedtak.routes.person.PersonDTO

interface PersonService {
    fun hentPerson(ident: String): PersonDTO?
}