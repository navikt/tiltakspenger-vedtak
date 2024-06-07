package no.nav.tiltakspenger.vedtak.repository.behandling

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.mockk.mockk
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import org.junit.jupiter.api.Test

class FørstegangsbehandlingTest {
    private val saksbehandlerMedTilgang = ObjectMother.saksbehandler()

    @Test
    fun `det skal ikke være mulig å legge til antall dager i uken på en behandling som er iverksatt`() {
        val iverksattBehandling = ObjectMother.behandlingInnvilgetIverksatt()
        shouldThrowWithMessage<IllegalArgumentException>(
            "Kan ikke oppdatere antall dager i tiltak, feil tilstand ${iverksattBehandling.tilstand}",
        ) {
            iverksattBehandling.oppdaterAntallDager(
                tiltakId = mockk<TiltakId>(),
                nyPeriodeMedAntallDager = mockk<PeriodeMedVerdi<AntallDager>>(),
                saksbehandler = saksbehandlerMedTilgang,
            )
        }
    }

    @Test
    fun `det skal ikke være mulig å nullstille saksbehandlers opplysninger for antall dager i uken på en behandling som er iverksatt`() {
        val iverksattBehandling = ObjectMother.behandlingInnvilgetIverksatt()
        shouldThrowWithMessage<IllegalArgumentException>(
            "Kan ikke tilbakestille antall dager i tiltak, feil tilstand ${iverksattBehandling.tilstand}",
        ) {
            iverksattBehandling.tilbakestillAntallDager(
                tiltakId = mockk<TiltakId>(),
                saksbehandler = saksbehandlerMedTilgang,
            )
        }
    }
}
