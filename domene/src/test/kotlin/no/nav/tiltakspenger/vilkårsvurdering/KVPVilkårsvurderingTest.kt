package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.objectmothers.arenaTiltak
import no.nav.tiltakspenger.vedtak.Søknad
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class KVPVilkårsvurderingTest {

    @Test
    fun `En vilkårsvurdering har en søknad`() {

        val søknad = Søknad(
            id = SøknadId.random(),
            søknadId = "1234",
            journalpostId = "123",
            dokumentInfoId = "123",
            fornavn = null,
            etternavn = null,
            ident = "",
            deltarKvp = true,
            deltarIntroduksjonsprogrammet = false,
            introduksjonsprogrammetDetaljer = null,
            oppholdInstitusjon = null,
            typeInstitusjon = null,
            opprettet = null,
            barnetillegg = listOf(),
            tidsstempelHosOss = LocalDateTime.now(),
            tiltak = arenaTiltak(),
            trygdOgPensjon = listOf(),
            fritekst = null
        )

        val vurderingsperiode = Periode(1.januar(2022), 31.januar(2022))

        val kvpVilkårsvurdering =
            KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode)

        kvpVilkårsvurdering.vurderinger().first().kilde shouldBe "Søknad"
        kvpVilkårsvurdering.vurderinger().first().fom shouldBe 1.januar(2022)
        kvpVilkårsvurdering.vurderinger().first().tom shouldBe 31.januar(2022)
        kvpVilkårsvurdering.vurderinger().first().utfall shouldBe Utfall.IKKE_OPPFYLT

        kvpVilkårsvurdering.samletUtfall() shouldBe Utfall.IKKE_OPPFYLT

    }
}
