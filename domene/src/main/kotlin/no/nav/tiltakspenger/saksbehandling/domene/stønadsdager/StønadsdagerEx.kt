package no.nav.tiltakspenger.saksbehandling.domene.stønadsdager

import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import java.time.LocalDateTime

fun Tiltak.tilStønadsdagerRegisterSaksopplysning(): StønadsdagerSaksopplysning.Register =
    // B: Hvorfor kan deltagelsen være null fra tiltaksappen? Får vi null-verdier fra Arena eller Komet?
    if (antallDagerPerUke != null) {
        StønadsdagerSaksopplysning.Register(
            tiltakNavn = gjennomføring.typeNavn,
            eksternId = eksternId,
            // Vi får per nå antall dager per uke, men ønsker å ha antall dager per meldeperiode.
            // Ettersom vi kan få desimaler fra komet gjør vi denne om til en int etter sammenleggingen.
            antallDager = (antallDagerPerUke * 2).toIntPrecise(),
            periode = deltakelsesperiode,
            kilde = kilde,
            tidsstempel = LocalDateTime.now(),
        )
    } else if (deltakelseProsent != null) {
        StønadsdagerSaksopplysning.Register(
            tiltakNavn = gjennomføring.typeNavn,
            eksternId = eksternId,
            // B: Så på tidligere kode som gjorde dette, kan deltakelseprosent være noe annet enn 100?
            antallDager = if (deltakelseProsent == 100f) 5 else throw IllegalStateException("Forventet 100% deltakelse. Vi støtter ikke lavere prosenter enn dette i MVP."),
            periode = deltakelsesperiode,
            kilde = kilde,
            tidsstempel = LocalDateTime.now(),
        )
    } else {
        throw IllegalStateException("Antall dager per uke og deltakelseprosent bør ikke være null samtidig. Da må vi i så fall legge til støtte for det etter MVP.")
    }

private fun Float.toIntPrecise() =
    if (this % 1 == 0f) this.toInt() else throw IllegalStateException("Forventet et heltall, men var $this")
