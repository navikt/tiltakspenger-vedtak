package no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad

import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import java.time.LocalDate
import java.util.UUID

fun stønadStatistikkMapper(sak: SakDetaljer, vedtak: Vedtak): StatistikkStønadDTO {
    return StatistikkStønadDTO(
        id = UUID.randomUUID(),
        brukerId = "brukerId",

        sakId = sak.id.toString(),
        saksnummer = sak.saksnummer.toString(),
        // vår sak har ikke resultat, så bruker vedtak sin resultat
        resultat = vedtak.vedtaksType.navn,
        // Hva skal vi bruke som sakDato?
        sakDato = LocalDate.now(),
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
        vedtakDato = vedtak.vedtaksdato.toLocalDate(),
        vedtakFom = vedtak.periode.fraOgMed,
        vedtakTom = vedtak.periode.tilOgMed,
    )
}
