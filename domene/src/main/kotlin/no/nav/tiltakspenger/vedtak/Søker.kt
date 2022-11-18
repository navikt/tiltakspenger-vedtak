package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.domene.nå
import no.nav.tiltakspenger.felles.SøkerId
import java.time.LocalDateTime

sealed class Søker(
    val søkerId: SøkerId,
    val ident: String,
    val tidsstempel: LocalDateTime,
    val opprettet: LocalDateTime,
) {
    class Init(
        søkerId: SøkerId,
        ident: String,
        tidsstempel: LocalDateTime = nå(),
        opprettet: LocalDateTime = nå(),
    ) : Søker(
        søkerId = søkerId,
        ident = ident,
        tidsstempel = tidsstempel,
        opprettet = opprettet,
    ) {
    }

    class Opprettet(
        søkerId: SøkerId,
        ident: String,
        tidsstempel: LocalDateTime,
        opprettet: LocalDateTime,
        val fornavn: String,
        val mellomnavn: String,
        val etternavn: String,
        val skjermet: Boolean,
        val fortrolig: Boolean,
        val strengtFortrolig: Boolean,
        val strengtFortroligUtland: Boolean,
        val innsendinger: List<Innsending>,
    ) : Søker(
        søkerId = søkerId,
        ident = ident,
        tidsstempel = tidsstempel,
        opprettet = opprettet,
    ) {

    }
}
