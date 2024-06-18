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

    @Test
    fun `erOpplysningFraSøknadAvklart skal returnere true hvis opplysningen om kravdato fra søknaden er den som er avklart`() {
        val kravdatoSaksopplysninger = kravdatoSaksopplysninger(kravdatoSaksopplysningFraSaksbehandler = null).avklar()
        kravdatoSaksopplysninger.erOpplysningFraSøknadAvklart() shouldBe true
    }

    @Test
    fun `erOpplysningFraSøknadAvklart skal returnere false hvis opplysningen om kravdato fra søknaden ikke er den som er avklart`() {
        val kravdatoSaksopplysninger = kravdatoSaksopplysninger().avklar()
        kravdatoSaksopplysninger.erOpplysningFraSøknadAvklart() shouldBe false
    }

    @Test
    fun `erOpplysningFraSaksbehandlerAvklart skal returnere true hvis opplysningen om kravdato fra saksbehandler er den som er avklart`() {
        val kravdatoSaksopplysninger = kravdatoSaksopplysninger().avklar()
        kravdatoSaksopplysninger.erOpplysningFraSaksbehandlerAvklart() shouldBe true
    }

    @Test
    fun `erOpplysningFraSaksbehandlerAvklart skal returnere false hvis opplysningen om kravdato fra saksbehandler ikke er den som er avklart`() {
        val kravdatoSaksopplysninger = kravdatoSaksopplysninger(kravdatoSaksopplysningFraSaksbehandler = null).avklar()
        kravdatoSaksopplysninger.erOpplysningFraSaksbehandlerAvklart() shouldBe false
    }
}
