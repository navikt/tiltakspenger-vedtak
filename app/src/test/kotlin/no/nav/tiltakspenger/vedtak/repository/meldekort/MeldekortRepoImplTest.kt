package no.nav.tiltakspenger.vedtak.repository.meldekort

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.common.getOrFail
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import no.nav.tiltakspenger.vedtak.db.persisterIverksattFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import org.junit.jupiter.api.Test

class MeldekortRepoImplTest {
    @Test
    fun `kan lagre og hente`() {
        withMigratedDb { testDataHelper ->
            val sak = testDataHelper.persisterIverksattFørstegangsbehandling()
            val meldekort =
                ObjectMother.utfyltMeldekort(
                    sakId = sak.id,
                    rammevedtakId = sak.rammevedtak!!.id,
                    fnr = sak.fnr,
                    saksnummer = sak.saksnummer,
                    antallDagerForMeldeperiode = sak.rammevedtak!!.antallDagerPerMeldeperiode,
                )
            val nesteMeldekort = meldekort.opprettNesteMeldekort(
                utfallsperioder = Periodisering(
                    AvklartUtfallForPeriode.OPPFYLT,
                    Periode(
                        fraOgMed = meldekort.periode.fraOgMed.plusWeeks(2),
                        tilOgMed = meldekort.periode.tilOgMed.plusWeeks(2),
                    ),
                ),
            ).getOrFail()
            val meldekortRepo = testDataHelper.meldekortRepo
            meldekortRepo.lagre(meldekort)
            val hentForMeldekortId = testDataHelper.sessionFactory.withSession {
                MeldekortPostgresRepo.hentForMeldekortId(meldekort.id, it)!!
            }
            hentForMeldekortId shouldBe meldekort
            meldekortRepo.lagre(nesteMeldekort)
            val hentForMeldekortId2 =
                testDataHelper.sessionFactory.withSession {
                    MeldekortPostgresRepo.hentForMeldekortId(
                        nesteMeldekort.id,
                        it,
                    )
                }
            hentForMeldekortId2 shouldBe nesteMeldekort
        }
    }
}
