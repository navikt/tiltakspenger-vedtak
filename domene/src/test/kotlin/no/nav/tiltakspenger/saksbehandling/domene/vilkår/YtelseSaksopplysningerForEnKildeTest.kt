package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import io.kotest.assertions.throwables.shouldThrow
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.desember
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class YtelseSaksopplysningerForEnKildeTest {
    @Test
    fun `sjekk at man kan opprette tomme saksopplysninger for en saksbehandler`() {
        val fom = 1.januar(2024)
        val tom = 31.januar(2024)

        YtelseSaksopplysningerForEnKilde(
            kilde = Kilde.SAKSB,
            periode = Periode(fom, tom),
            saksopplysninger = emptyList(),
            tidspunkt = LocalDateTime.now(),
        )
    }

    @Test
    fun `sjekk at hvis saksbehandler fyller ut saksopplysninger, skal de ikke starte tidligere enn perioden`() {
        val fom = 1.januar(2024)
        val tom = 31.januar(2024)

        shouldThrow<IllegalArgumentException> {
            YtelseSaksopplysningerForEnKilde(
                kilde = Kilde.SAKSB,
                periode = Periode(fom, tom),
                saksopplysninger = listOf(
                    YtelseSaksopplysning(
                        periode = Periode(fra = 1.desember(2023), til = 31.januar(2024)),
                        kilde = Kilde.SAKSB,
                        vilkår = Vilkår.AAP,
                        detaljer = "",
                        saksbehandler = "Z123456",
                        harYtelse = false,
                    ),
                ),
                tidspunkt = LocalDateTime.now(),
            )
        }
    }

    @Test
    fun `sjekk at hvis saksbehandler fyller ut saksopplysninger, skal de ikke slutte senere enn perioden`() {
        val fom = 1.januar(2024)
        val tom = 31.januar(2024)

        shouldThrow<IllegalArgumentException> {
            YtelseSaksopplysningerForEnKilde(
                kilde = Kilde.SAKSB,
                periode = Periode(fom, tom),
                saksopplysninger = listOf(
                    YtelseSaksopplysning(
                        periode = Periode(fra = 1.januar(2023), til = 28.februar(2024)),
                        kilde = Kilde.SAKSB,
                        vilkår = Vilkår.AAP,
                        detaljer = "",
                        saksbehandler = "Z123456",
                        harYtelse = false,
                    ),
                ),
                tidspunkt = LocalDateTime.now(),
            )
        }
    }

    @Test
    fun `sjekk at saksopplysninger ikke overlapper`() {
        val fom = 1.januar(2024)
        val tom = 31.januar(2024)

        shouldThrow<IllegalArgumentException> {
            YtelseSaksopplysningerForEnKilde(
                kilde = Kilde.SAKSB,
                periode = Periode(fom, tom),
                saksopplysninger = listOf(
                    YtelseSaksopplysning(
                        periode = Periode(fra = 1.januar(2024), til = 16.januar(2024)),
                        kilde = Kilde.SAKSB,
                        vilkår = Vilkår.AAP,
                        detaljer = "",
                        saksbehandler = "Z123456",
                        harYtelse = false,
                    ),
                    YtelseSaksopplysning(
                        periode = Periode(fra = 15.januar(2024), til = 31.januar(2024)),
                        kilde = Kilde.SAKSB,
                        vilkår = Vilkår.AAP,
                        detaljer = "",
                        saksbehandler = "Z123456",
                        harYtelse = false,
                    ),
                ),
                tidspunkt = LocalDateTime.now(),
            )
        }
    }

    @Test
    fun `sjekk at det ikke er huller i listen`() {
        val fom = 1.januar(2024)
        val tom = 31.januar(2024)

        shouldThrow<IllegalArgumentException> {
            YtelseSaksopplysningerForEnKilde(
                kilde = Kilde.SAKSB,
                periode = Periode(fom, tom),
                saksopplysninger = listOf(
                    YtelseSaksopplysning(
                        periode = Periode(fra = 1.januar(2024), til = 16.januar(2024)),
                        kilde = Kilde.SAKSB,
                        vilkår = Vilkår.AAP,
                        detaljer = "",
                        saksbehandler = "Z123456",
                        harYtelse = false,
                    ),
                    YtelseSaksopplysning(
                        periode = Periode(fra = 18.januar(2024), til = 31.januar(2024)),
                        kilde = Kilde.SAKSB,
                        vilkår = Vilkår.AAP,
                        detaljer = "",
                        saksbehandler = "Z123456",
                        harYtelse = false,
                    ),
                ),
                tidspunkt = LocalDateTime.now(),
            )
        }
    }
}
