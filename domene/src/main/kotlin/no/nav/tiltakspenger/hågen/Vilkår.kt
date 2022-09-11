package no.nav.tiltakspenger.hågen

interface Vilkår {
    fun paragraf(): String
    fun fellesMed(vilkår: Vilkår): Vilkår = this //TODO
}

interface Inngangsvilkår : Vilkår

// Hva er evt fordelen ved å  ha det som objects?
enum class InngangsvilkårEnum(private val paragraf: String) : Inngangsvilkår {
    ErOver18År("7.1"),
    KVP("7.3");

    override fun paragraf(): String = paragraf
}

object ErOver18År : Inngangsvilkår {
    override fun paragraf(): String = "7.1"
}

object KVP : Inngangsvilkår {
    override fun paragraf(): String = "7.3"
}

object StatligeYtelser : Inngangsvilkår {
    override fun paragraf(): String = "7"
}
