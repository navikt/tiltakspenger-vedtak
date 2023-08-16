package no.nav.tiltakspenger.vedtak.service

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.felles.april
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother.brukerTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vedtak.service.sak.SakServiceImpl
import org.junit.jupiter.api.Test

internal class SakServiceTest {

    val sakRepo: SakRepo = mockk()
    val sakService = SakServiceImpl(sakRepo)

    @Test
    fun `mottak av søknad happypath`() {
        every { sakRepo.findByFnrAndPeriode(any(), any()) } returns emptyList()
        every { sakRepo.save(any()) } returnsArgument 0

        val søknad = nySøknadMedBrukerTiltak()
        val sak = sakService.motta(søknad)

        sak shouldNotBe null // TODO sjekk flere felter i sak
    }

    @Test
    fun `mottak av ny søknad med ikke overlappende eksisterende søknad innenfor karenstid`() {
        every { sakRepo.findByFnrAndPeriode(any(), any()) } returns emptyList()
        every { sakRepo.save(any()) } returnsArgument 0

        val søknad = nySøknadMedBrukerTiltak(
            tiltak = brukerTiltak(
                startdato = 1. januar(2023),
                sluttdato = 31. januar(2023),
            )
        )
        val sak = sakService.motta(søknad)

        every { sakRepo.findByFnrAndPeriode(any(), any()) } returns listOf(sak)

        val søknad2 = nySøknadMedBrukerTiltak(
            tiltak = brukerTiltak(
                startdato = 1. mars(2023),
                sluttdato = 31. mars(2023),
            )
        )
        val sak2 = sakService.motta(søknad2)

        sak2.behandlinger.size shouldBe 2
        sak.id shouldBe sak2.id
    }

    @Test
    fun `mottak av ny søknad med ikke overlappende eksisterende søknad utenfor karenstid`() {
        every { sakRepo.findByFnrAndPeriode(any(), any()) } returns emptyList()
        every { sakRepo.save(any()) } returnsArgument 0

        val søknad = nySøknadMedBrukerTiltak(
            tiltak = brukerTiltak(
                startdato = 1. januar(2023),
                sluttdato = 31. januar(2023),
            )
        )
        val sak = sakService.motta(søknad)

        every { sakRepo.findByFnrAndPeriode(any(), any()) } returns listOf(sak)

        val søknad2 = nySøknadMedBrukerTiltak(
            tiltak = brukerTiltak(
                startdato = 1. april(2023),
                sluttdato = 30. april(2023),
            )
        )
        val sak2 = sakService.motta(søknad2)

        sak.behandlinger.size shouldBe 1
        sak2.behandlinger.size shouldBe 1
        sak.id shouldNotBe sak2.id
    }
}
