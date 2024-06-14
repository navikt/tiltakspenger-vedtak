package no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.ObjectMother.kravdatoSaksopplysninger
import org.junit.jupiter.api.Test

internal class KravdatoSaksopplysningerTest {
    @Test
    fun `når man avklarer kravdato, og saksbehandler har lagt inn en kravdato manuelt, skal man få en KravdatoSaksopplysninger tilbake med avklartKravdato satt til det samme som saksbehandleren har satt`() {
        val kravdatoSaksopplysninger = kravdatoSaksopplysninger()
        val avklarteSaksopplysninger = kravdatoSaksopplysninger.avklar().avklartKravdatoSaksopplysning
        avklarteSaksopplysninger shouldBe kravdatoSaksopplysninger.kravdatoSaksopplysningFraSaksbehandler
    }

    @Test
    fun `når man avklarer kravdato, og saksbehandler ikke har lagt inn en kravdato manuelt, skal man få en KravdatoSaksopplysninger tilbake med avklartKravdato satt til det som er registrert fra søknaden`() {
        val kravdatoSaksopplysninger = kravdatoSaksopplysninger(kravdatoSaksopplysningFraSaksbehandler = null)
        val avklarteSaksopplysninger = kravdatoSaksopplysninger.avklar().avklartKravdatoSaksopplysning
        avklarteSaksopplysninger shouldBe kravdatoSaksopplysninger.kravdatoSaksopplysningFraSøknad
    }
}
