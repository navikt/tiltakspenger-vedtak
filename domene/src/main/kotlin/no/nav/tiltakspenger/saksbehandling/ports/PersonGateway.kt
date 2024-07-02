package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger

interface PersonGateway {
    // TODO jah: Bør vi bruke noen annet enn String for ident?
    suspend fun hentPerson(ident: String): List<Personopplysninger>
}
