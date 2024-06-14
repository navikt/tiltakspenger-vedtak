package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import java.time.LocalDate

interface KravdatoSaksopplysningMother {
    fun kravdatoSaksopplysningFraSøknad(
        kravdato: LocalDate = 1.januar(2026),
    ): KravdatoSaksopplysning =
        KravdatoSaksopplysning(
            kravdato = kravdato,
            kilde = Kilde.SØKNAD,
        )

    fun kravdatoSaksopplysningFraSaksbehandler(
        kravdato: LocalDate = 2.januar(2026),
        saksbehandlerIdent: String = "test",
    ): KravdatoSaksopplysning =
        KravdatoSaksopplysning(
            kravdato = kravdato,
            kilde = Kilde.SAKSB,
            saksbehandlerIdent = saksbehandlerIdent,
        )

    fun kravdatoSaksopplysninger(
        kravdatoSaksopplysningFraSaksbehandler: KravdatoSaksopplysning? = kravdatoSaksopplysningFraSaksbehandler(),
    ): KravdatoSaksopplysninger =
        KravdatoSaksopplysninger(
            kravdatoSaksopplysningFraSøknad = kravdatoSaksopplysningFraSøknad(),
            kravdatoSaksopplysningFraSaksbehandler = kravdatoSaksopplysningFraSaksbehandler,
        )
}
