package no.nav.tiltakspenger.vilkårsvurdering.kategori

import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

// TODO: samletUtfall må fjernes/endres, den er potensielt misvisende.
// Hvis man har en liste med vurderinger, så kan de ikke nødvendigvis reduseres til ett utfall
// Et utfall på IkkeOppfylt må isåfall dekke hele vurderingsperioden,
// gjør den ikke det så er jo kanskje det korrekte utfallet "DelvisOppfylt".
// Dette er tatt håndt om i klassen Konklusjon.
interface VilkårsvurderingKategori {
    fun vilkår(): Vilkår
    fun samletUtfall(): Utfall
    fun vurderinger(): List<Vurdering>
}
