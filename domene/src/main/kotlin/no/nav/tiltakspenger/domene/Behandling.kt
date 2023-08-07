package no.nav.tiltakspenger.domene

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Søknad
import java.util.UUID

data class Behandling(
    val id: UUID,
    val søknader: List<Søknad>,
    val vurderingsperiode: Periode,
    val innhentedeRådata: Innsending,
    val avklarteSaksopplysninger: List<Saksopplysning>,
    val vilkårsvurderinger: List<TODOVilkårsvurderinger>

)
