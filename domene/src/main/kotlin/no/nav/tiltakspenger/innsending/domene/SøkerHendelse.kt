package no.nav.tiltakspenger.innsending.domene

interface ISøkerHendelse :
    KontekstLogable,
    IAktivitetslogg {
    val aktivitetslogg: Aktivitetslogg
    fun ident(): String
    fun toLogString(): String
}

abstract class SøkerHendelse protected constructor(
    override val aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : IAktivitetslogg by aktivitetslogg, ISøkerHendelse {

    init {
        aktivitetslogg.addKontekst(this)
    }

    override fun opprettKontekst(): Kontekst {
        return this.javaClass.canonicalName.split('.').last().let {
            Kontekst(it, mapOf("ident" to ident()))
        }
    }

    override fun toLogString() = aktivitetslogg.toString()
}
