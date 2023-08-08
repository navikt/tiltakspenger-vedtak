package no.nav.tiltakspenger.vedtak.service

import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vedtak.service.sak.SakServiceImpl
import org.junit.jupiter.api.Test

internal class SakServiceTest {

    val sakRepo: SakRepo = mockk()
    val sakService = SakServiceImpl(sakRepo)

    @Test
    fun `mottak av søknad happypath`() {

        val søknad = nySøknadMedBrukerTiltak()
//        val periode = Periode(søknad.tiltak!!.startdato, søknad.tiltak!!.sluttdato!!)
        val sak = sakService.motta(søknad)

        sak shouldNotBe null
    }

//    @Test
//    fun `mottak av søknad uten periode på tiltak`() {
//    }

    }
