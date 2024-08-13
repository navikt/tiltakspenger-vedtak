package no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad

import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import java.time.LocalDate
import java.util.UUID

// dette navnet er bare en placeholder. Bytt til beskrivende navn
fun stønadStatistikkMapper(sak: SakDetaljer, vedtak: Vedtak): StatistikkStønadDTO {
    return StatistikkStønadDTO(
        id = UUID.randomUUID(),
        sakId = sak.id.toString(),
        brukerId = "brukerId",
        resultat = "resultat",
        sakDato = LocalDate.now(),
        // sak har ikke periode lengre, så bruker vedtak sin periode
        sakFraDato = vedtak.periode.fraOgMed,
        sakTilDato = vedtak.periode.tilOgMed,
        søknadId = vedtak.behandling.søknad.id.toString(),
        ytelse = "IND",
        opplysning = "",
        søknadDato = vedtak.behandling.søknad.opprettet.toLocalDate(),
        søknadFraDato = vedtak.behandling.søknad.tiltak.deltakelseFom,
        søknadTilDato = vedtak.behandling.søknad.tiltak.deltakelseTom,
        vedtakId = vedtak.id.toString(),
    )
}
