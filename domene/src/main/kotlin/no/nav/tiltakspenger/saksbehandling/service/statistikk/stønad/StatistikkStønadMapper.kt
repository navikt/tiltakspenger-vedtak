package no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad

import no.nav.tiltakspenger.saksbehandling.domene.sak.TynnSak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

fun stønadStatistikkMapper(
    sak: TynnSak,
    vedtak: Rammevedtak,
): StatistikkStønadDTO {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    return StatistikkStønadDTO(
        id = UUID.randomUUID(),
        brukerId = sak.fnr.verdi,

        sakId = sak.id.toString(),
        saksnummer = sak.saksnummer.toString(),
        // vår sak har ikke resultat, så bruker vedtak sin resultat
        resultat = vedtak.vedtaksType.navn,
        sakDato = LocalDate.parse(sak.saksnummer.toString().substring(0, 8), formatter),
        // sak har ikke periode lengre, så bruker vedtak sin periode
        sakFraDato = vedtak.periode.fraOgMed,
        sakTilDato = vedtak.periode.tilOgMed,
        ytelse = "IND",

        søknadId = vedtak.behandling.søknad.id.toString(),
        opplysning = "",
        søknadDato = vedtak.behandling.søknad.opprettet.toLocalDate(),
        søknadFraDato = vedtak.behandling.søknad.tiltak.deltakelseFom,
        søknadTilDato = vedtak.behandling.søknad.tiltak.deltakelseTom,

        vedtakId = vedtak.id.toString(),
        vedtaksType = "Ny Rettighet",
        // TODO() Post-mvp: Denne skal kanskje egentlig være datoen fra brevet. Ta en prat med statistikk.
        vedtakDato = vedtak.opprettet.toLocalDate(),
        vedtakFom = vedtak.periode.fraOgMed,
        vedtakTom = vedtak.periode.tilOgMed,
    )
}
