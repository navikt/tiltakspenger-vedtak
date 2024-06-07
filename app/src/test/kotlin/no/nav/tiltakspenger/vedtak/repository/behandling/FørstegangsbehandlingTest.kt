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
    private val saksbehandlerUtenTilgang = ObjectMother.saksbehandlerUtenTilgang()

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
    fun `det skal ikke være mulig å oppdatere antall dager uten saksbehandler-tilgang`() {
        val vilkårsvurdertBehandling = ObjectMother.behandlingVilkårsvurdertInnvilget()
        shouldThrowWithMessage<IllegalStateException>(
            "Man kan ikke oppdatere antall dager uten å være saksbehandler eller admin",
        ) {
            vilkårsvurdertBehandling.oppdaterAntallDager(
                tiltakId = mockk<TiltakId>(),
                nyPeriodeMedAntallDager = mockk<PeriodeMedVerdi<AntallDager>>(),
                saksbehandler = saksbehandlerUtenTilgang,
            )
        }
    }

    @Test
    fun `det skal ikke være mulig å tilbakestille saksbehandlers opplysninger for antall dager i uken på en behandling som er iverksatt`() {
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

    @Test
    fun `det skal ikke være mulig å tilbakestille antall dager uten saksbehandler-tilgang`() {
        val vilkårsvurdertBehandling = ObjectMother.behandlingVilkårsvurdertInnvilget()
        shouldThrowWithMessage<IllegalStateException>(
            "Man kan ikke tilbakestille antall dager uten å være saksbehandler eller admin",
        ) {
            vilkårsvurdertBehandling.tilbakestillAntallDager(
                tiltakId = mockk<TiltakId>(),
                saksbehandler = saksbehandlerUtenTilgang,
            )
        }
    }
}
