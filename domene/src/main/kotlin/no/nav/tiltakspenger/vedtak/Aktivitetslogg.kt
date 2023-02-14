package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.domene.nå
import java.time.LocalDateTime

// Understands issues that arose when analyzing a JSON message
// Implements Collecting Parameter in Refactoring by Martin Fowler
// Implements Visitor pattern to traverse the messages
open class Aktivitetslogg(
    private var forelder: IAktivitetslogg? = null,
    private val aktiviteter: MutableList<Aktivitet> = mutableListOf(),
) : IAktivitetslogg {
    // Kunne/burde dette vært en stack heller enn en list? (https://stackoverflow.com/questions/46900048/how-can-i-use-stack-in-kotlin)
    private val kontekster = mutableListOf<KontekstLogable>() // Doesn't need serialization

    internal fun MutableList<Kontekst>.snapshot(): List<Kontekst> = this.toList()

    override fun aktiviteter(): List<Aktivitet> = aktiviteter.toList()

    fun accept(visitor: IAktivitetsloggVisitor) {
        visitor.preVisitAktivitetslogg(this)
        aktiviteter.forEach { it.accept(visitor) }
        visitor.postVisitAktivitetslogg(this)
    }

    override fun info(melding: String) {
        add(Aktivitet.Info(kontekster.toKontekst(), melding))
    }

    override fun warn(melding: String) {
        add(Aktivitet.Warn(kontekster.toKontekst(), melding))
    }

    override fun behov(type: Aktivitet.Behov.Behovtype, melding: String, detaljer: Map<String, Any>) {
        add(Aktivitet.Behov(type, kontekster.toKontekst(), melding, detaljer))
    }

    override fun error(melding: String) {
        add(Aktivitet.Error(kontekster.toKontekst(), melding))
    }

    override fun severe(melding: String) {
        add(Aktivitet.Severe(kontekster.toKontekst(), melding))

        throw AktivitetException(this)
    }

    override fun add(aktivitet: Aktivitet) {
        this.aktiviteter.add(aktivitet)
        forelder?.let { forelder?.add(aktivitet) }
    }

    private fun MutableList<KontekstLogable>.toKontekst() = this.map { it.opprettKontekst() }

    override fun hasMessages() = info().isNotEmpty() || hasWarnings()

    override fun hasWarnings() = warn().isNotEmpty() || hasBehov()

    override fun hasBehov() = behov().isNotEmpty() || hasErrors()

    override fun hasErrors() = error().isNotEmpty() || severe().isNotEmpty()

    override fun barn() = Aktivitetslogg(this)

    override fun toString() = this.aktiviteter.map { it.inOrder() }.joinToString(separator = "\n", prefix = "\n") { it }

    override fun aktivitetsteller() = aktiviteter.size

    override fun addKontekst(kontekst: KontekstLogable) {
        kontekster.add(kontekst)
    }

    private fun setForelder(innsending: Innsending) {
        forelder = innsending.aktivitetslogg
    }

    override fun setForelderAndAddKontekst(innsending: Innsending) {
        setForelder(innsending)
        addKontekst(innsending)
    }

    internal fun logg(kontekst: KontekstLogable): Aktivitetslogg {
        return Aktivitetslogg(this).also {
            it.aktiviteter.addAll(this.aktiviteter.filter { aktivitet -> kontekst in aktivitet })
        }
    }

    // Hva gjør egentlig denne? Den er åpenbart ikke en getter på this.kontekster..
    override fun kontekster(): List<Aktivitetslogg> {
        val groupBy: Map<Map<String, String>, List<Aktivitet>> =
            aktiviteter.groupBy { aktivitet -> aktivitet.konteksterAvTypeAsMap(typer = listOf("Søker")) }

        val aktivitetListeListe: List<List<Aktivitet>> = groupBy.map { it.value }

        return aktivitetListeListe.map { aktivitetListe ->
            Aktivitetslogg(this).apply {
                aktiviteter.addAll(
                    aktivitetListe,
                )
            }
        }
    }

    private fun info() = Aktivitet.Info.filter(aktiviteter)
    private fun warn() = Aktivitet.Warn.filter(aktiviteter)
    override fun behov() = Aktivitet.Behov.filter(aktiviteter)
    private fun error() = Aktivitet.Error.filter(aktiviteter)
    private fun severe() = Aktivitet.Severe.filter(aktiviteter)

    class AktivitetException internal constructor(private val aktivitetslogg: Aktivitetslogg) :
        RuntimeException(aktivitetslogg.toString()) {
        fun kontekst(): Map<String, String> = aktivitetslogg.kontekster.fold(mutableMapOf()) { result, kontekst ->
            result.apply { putAll(kontekst.opprettKontekst().kontekstMap) }
        }

        fun aktivitetslogg() = aktivitetslogg
    }

    sealed class Aktivitet(
        val alvorlighetsgrad: Int,
        val label: Char,
        val melding: String,
        val tidsstempel: LocalDateTime,
        val kontekster: List<Kontekst>,
        val persistert: Boolean,
    ) : Comparable<Aktivitet> {

        fun alleKonteksterAsMap(): Map<String, String> =
            kontekster.fold(mutableMapOf()) { result, spesifikkKontekst -> result.apply { putAll(spesifikkKontekst.kontekstMap) } }

        internal fun konteksterAvTypeAsMap(typer: List<String>): Map<String, String> =
            kontekster.let { spesifikkKontekst -> if (typer.isEmpty()) spesifikkKontekst else spesifikkKontekst.filter { it.kontekstType in typer } }
                .fold(mutableMapOf()) { result, spesifikkKontekst -> result.apply { putAll(spesifikkKontekst.kontekstMap) } }

        override fun compareTo(other: Aktivitet) = this.tidsstempel.compareTo(other.tidsstempel)
            .let { if (it == 0) other.alvorlighetsgrad.compareTo(this.alvorlighetsgrad) else it }

        internal fun inOrder() = label + "\t" + this.toString()

        override fun toString() = tidsstempel.toString() + "\t" + melding + meldingerString()

        private fun meldingerString(): String {
            return kontekster.joinToString(separator = " ") { "(${it.melding()})" }
        }

        internal abstract fun accept(visitor: IAktivitetsloggVisitor)

        operator fun contains(kontekst: KontekstLogable) = kontekst.opprettKontekst() in kontekster
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Aktivitet

            if (alvorlighetsgrad != other.alvorlighetsgrad) return false
            if (label != other.label) return false
            if (melding != other.melding) return false
            if (tidsstempel != other.tidsstempel) return false
            if (kontekster != other.kontekster) return false

            return true
        }

        override fun hashCode(): Int {
            var result = alvorlighetsgrad
            result = 31 * result + label.hashCode()
            result = 31 * result + melding.hashCode()
            result = 31 * result + tidsstempel.hashCode()
            result = 31 * result + kontekster.hashCode()
            return result
        }

        class Info(
            kontekster: List<Kontekst>,
            melding: String,
            tidsstempel: LocalDateTime = nå(),
            persistert: Boolean = false,
        ) : Aktivitet(0, 'I', melding, tidsstempel, kontekster, persistert) {
            companion object {
                internal fun filter(aktiviteter: List<Aktivitet>): List<Info> {
                    return aktiviteter.filterIsInstance<Info>()
                }
            }

            override fun accept(visitor: IAktivitetsloggVisitor) {
                visitor.visitInfo(kontekster, this, melding, tidsstempel)
            }
        }

        class Warn(
            kontekster: List<Kontekst>,
            melding: String,
            tidsstempel: LocalDateTime = nå(),
            persistert: Boolean = false,
        ) : Aktivitet(25, 'W', melding, tidsstempel, kontekster, persistert) {
            companion object {
                internal fun filter(aktiviteter: List<Aktivitet>): List<Warn> {
                    return aktiviteter.filterIsInstance<Warn>()
                }
            }

            override fun accept(visitor: IAktivitetsloggVisitor) {
                visitor.visitWarn(kontekster, this, melding, tidsstempel)
            }
        }

        class Behov(
            val type: Behovtype,
            kontekster: List<Kontekst>,
            melding: String,
            val detaljer: Map<String, Any> = emptyMap(),
            tidsstempel: LocalDateTime = nå(),
            persistert: Boolean = false,
        ) : Aktivitet(50, 'N', melding, tidsstempel, kontekster, persistert) {
            companion object {
                internal fun filter(aktiviteter: List<Aktivitet>): List<Behov> {
                    return aktiviteter.filterIsInstance<Behov>()
                }
            }

            fun detaljer() = detaljer

            override fun accept(visitor: IAktivitetsloggVisitor) {
                visitor.visitBehov(kontekster, this, type, melding, detaljer, tidsstempel)
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                if (!super.equals(other)) return false

                other as Behov

                if (type != other.type) return false
                if (detaljer != other.detaljer) return false

                return true
            }

            override fun hashCode(): Int {
                var result = super.hashCode()
                result = 31 * result + type.hashCode()
                result = 31 * result + detaljer.hashCode()
                return result
            }

            // Disse burde vi vel kunne endre til Camelcase slik at ktlint blir happy
            @Suppress("ktlint:enum-entry-name-case")
            enum class Behovtype {
                personopplysninger, skjerming, arenatiltak, arenaytelser, fpytelser, uføre,
            }
        }

        class Error(
            kontekster: List<Kontekst>,
            melding: String,
            tidsstempel: LocalDateTime = nå(),
            persistert: Boolean = false,
        ) : Aktivitet(75, 'E', melding, tidsstempel, kontekster, persistert) {
            companion object {
                internal fun filter(aktiviteter: List<Aktivitet>): List<Error> {
                    return aktiviteter.filterIsInstance<Error>()
                }
            }

            override fun accept(visitor: IAktivitetsloggVisitor) {
                visitor.visitError(kontekster, this, melding, tidsstempel)
            }
        }

        class Severe(
            kontekster: List<Kontekst>,
            melding: String,
            tidsstempel: LocalDateTime = nå(),
            persistert: Boolean = false,
        ) : Aktivitet(100, 'S', melding, tidsstempel, kontekster, persistert) {
            companion object {
                internal fun filter(aktiviteter: List<Aktivitet>): List<Severe> {
                    return aktiviteter.filterIsInstance<Severe>()
                }
            }

            override fun accept(visitor: IAktivitetsloggVisitor) {
                visitor.visitSevere(kontekster, this, melding, tidsstempel)
            }
        }
    }
}

interface IAktivitetslogg {
    fun info(melding: String)
    fun warn(melding: String)
    fun behov(type: Aktivitetslogg.Aktivitet.Behov.Behovtype, melding: String, detaljer: Map<String, Any> = emptyMap())
    fun error(melding: String)
    fun severe(melding: String)

    fun hasMessages(): Boolean
    fun hasWarnings(): Boolean
    fun hasBehov(): Boolean
    fun hasErrors(): Boolean
    fun aktivitetsteller(): Int
    fun behov(): List<Aktivitetslogg.Aktivitet.Behov>
    fun barn(): Aktivitetslogg
    fun addKontekst(kontekst: KontekstLogable)
    fun setForelderAndAddKontekst(innsending: Innsending)
    fun kontekster(): List<IAktivitetslogg>
    fun aktiviteter(): List<Aktivitetslogg.Aktivitet>
    fun add(aktivitet: Aktivitetslogg.Aktivitet)
}

interface IAktivitetsloggVisitor {
    fun preVisitAktivitetslogg(aktivitetslogg: Aktivitetslogg) {}
    fun visitInfo(
        kontekster: List<Kontekst>,
        aktivitet: Aktivitetslogg.Aktivitet.Info,
        melding: String,
        tidsstempel: LocalDateTime,
    ) {
    }

    fun visitWarn(
        kontekster: List<Kontekst>,
        aktivitet: Aktivitetslogg.Aktivitet.Warn,
        melding: String,
        tidsstempel: LocalDateTime,
    ) {
    }

    fun visitBehov(
        kontekster: List<Kontekst>,
        aktivitet: Aktivitetslogg.Aktivitet.Behov,
        type: Aktivitetslogg.Aktivitet.Behov.Behovtype,
        melding: String,
        detaljer: Map<String, Any>,
        tidsstempel: LocalDateTime,
    ) {
    }

    fun visitError(
        kontekster: List<Kontekst>,
        aktivitet: Aktivitetslogg.Aktivitet.Error,
        melding: String,
        tidsstempel: LocalDateTime,
    ) {
    }

    fun visitSevere(
        kontekster: List<Kontekst>,
        aktivitet: Aktivitetslogg.Aktivitet.Severe,
        melding: String,
        tidsstempel: LocalDateTime,
    ) {
    }

    fun postVisitAktivitetslogg(aktivitetslogg: Aktivitetslogg) {}
}

interface KontekstLogable {
    fun opprettKontekst(): Kontekst
}

class Kontekst(val kontekstType: String, val kontekstMap: Map<String, String> = mapOf()) {
    internal fun melding() =
        kontekstType + kontekstMap.entries.joinToString(separator = ", ", prefix = " - ") { "${it.key}: ${it.value}" }

    override fun equals(other: Any?) = this === other || other is Kontekst && this.kontekstMap == other.kontekstMap

    override fun hashCode() = kontekstMap.hashCode()
}
