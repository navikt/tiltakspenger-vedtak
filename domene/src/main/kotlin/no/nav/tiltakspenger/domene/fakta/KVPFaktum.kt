package no.nav.tiltakspenger.domene.fakta

import no.nav.tiltakspenger.domene.Faktum
import no.nav.tiltakspenger.domene.FaktumKilde

data class KVPFaktum(val deltarKVP: Boolean, override val kilde: FaktumKilde) : Faktum
