package no.nav.tiltakspenger.vedtak

import java.time.LocalDateTime

class Skjerming(val ident: String, val skjerming: Boolean, val innhentet: LocalDateTime) : Tidsstempler {
    fun accept(visitor: SkjermingVisitor) {
        visitor.visitSkjerming(this)
    }

    override fun oppdatert() = innhentet
    
    override fun innhentet() = innhentet
}
