package no.nav.tiltakspenger.vedtak.repository.benk

import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt.Behandlingstype.FØRSTEGANGSBEHANDLING
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt.Behandlingstype.SØKNAD
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.SaksoversiktRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.toBehandlingsstatus

class SaksoversiktPostgresRepo(
    private val sessionFactory: PostgresSessionFactory,
) : SaksoversiktRepo {
    override fun hentAlle(
        sessionContext: SessionContext?,
    ): Saksoversikt {
        return sessionFactory.withSession(sessionContext) { session ->
            session.run(
                queryOf("select s.id søknadId, s.ident, b.id behandlingId, b.fom, b.tom, b.status, sak.saksnummer, b.saksbehandler, b.beslutter, b.sakId from søknad s left join behandling b on b.id = s.behandling_id left join sak on sak.id = b.sakId order by s.id, sak.saksnummer, b.id")
                    .map { row ->
                        val erFørstegangsbehandling = row.stringOrNull("behandlingId") != null
                        val id = row.stringOrNull("behandlingId")?.let { BehandlingId.fromString(it) }
                            ?: SøknadId.fromString(row.string("søknadId"))
                        val periode = if (erFørstegangsbehandling) {
                            Periode(
                                fraOgMed = row.localDate("fom"),
                                tilOgMed = row.localDate("tom"),
                            )
                        } else {
                            null
                        }
                        val beslutter = row.stringOrNull("beslutter")
                        val saksbehandler = row.stringOrNull("saksbehandler")
                        val status = if (erFørstegangsbehandling) {
                            BehandlingEllerSøknadForSaksoversikt.Status.Behandling(
                                row.string("status").toBehandlingsstatus(),
                            )
                        } else {
                            null
                        }
                        BehandlingEllerSøknadForSaksoversikt(
                            periode = periode,
                            status = if (erFørstegangsbehandling) status!! else BehandlingEllerSøknadForSaksoversikt.Status.Søknad,
                            behandlingstype = if (erFørstegangsbehandling) FØRSTEGANGSBEHANDLING else SØKNAD,
                            fnr = Fnr.fromString(row.string("ident")),
                            saksnummer = row.stringOrNull("saksnummer")?.let { Saksnummer(it) },
                            id = id,
                            saksbehandler = saksbehandler,
                            beslutter = beslutter,
                            sakId = row.stringOrNull("sakId")?.let { SakId.fromString(it) },
                        )
                    }
                    .asList,
            ).let {
                Saksoversikt(it)
            }
        }
    }
}
