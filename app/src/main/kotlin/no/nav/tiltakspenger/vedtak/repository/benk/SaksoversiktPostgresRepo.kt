package no.nav.tiltakspenger.vedtak.repository.benk

import kotliquery.queryOf
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt.Behandlingstype.FØRSTEGANGSBEHANDLING
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt.Behandlingstype.SØKNAD
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.SaksoversiktRepo
import no.nav.tiltakspenger.vedtak.repository.behandling.attesteringer.toAttesteringer
import no.nav.tiltakspenger.vedtak.repository.behandling.toBehandlingsstatus

class SaksoversiktPostgresRepo(
    private val sessionFactory: PostgresSessionFactory,
) : SaksoversiktRepo {
    override fun hentAlle(sessionContext: SessionContext?): Saksoversikt =
        sessionFactory.withSession(sessionContext) { session ->
            session
                .run(
                    queryOf(
                        //language=SQL
                        """
                        select s.id søknad_id,
                          s.ident,
                          s.opprettet,
                          b.id behandling_id,
                          b.fra_og_med,
                          b.til_og_med,
                          b.status,
                          sak.saksnummer,
                          b.saksbehandler,
                          b.beslutter,
                          b.attesteringer,
                          b.sak_id,
                          (b.vilkårssett -> 'kravfristVilkår' -> 'avklartSaksopplysning' ->> 'kravdato') as kravdato
                          
                        from søknad s 
                        left join behandling b on b.id = s.behandling_id 
                        left join sak on sak.id = b.sak_id 
                        order by s.id, sak.saksnummer, b.id
                        """.trimIndent(),
                    ).map { row ->
                        val erFørstegangsbehandling = row.stringOrNull("behandling_id") != null
                        val id =
                            row.stringOrNull("behandling_id")?.let { BehandlingId.fromString(it) }
                                ?: SøknadId.fromString(row.string("søknad_id"))
                        val periode =
                            if (erFørstegangsbehandling) {
                                Periode(
                                    fraOgMed = row.localDate("fra_og_med"),
                                    tilOgMed = row.localDate("til_og_med"),
                                )
                            } else {
                                null
                            }
                        // B: Vil kanskje bruke kravdato fra vilkåret på sikt, men bruker kun søknaden for nå
                        val opprettet = row.localDateTime("opprettet")
                        val beslutter = row.stringOrNull("beslutter")
                        val saksbehandler = row.stringOrNull("saksbehandler")
                        val status =
                            if (erFørstegangsbehandling) {
                                BehandlingEllerSøknadForSaksoversikt.Status.Behandling(
                                    row.string("status").toBehandlingsstatus(),
                                )
                            } else {
                                null
                            }
                        val attesteringer = if (erFørstegangsbehandling) row.string("attesteringer").toAttesteringer() else null
                        BehandlingEllerSøknadForSaksoversikt(
                            periode = periode,
                            status = if (erFørstegangsbehandling) status!! else BehandlingEllerSøknadForSaksoversikt.Status.Søknad,
                            underkjent = if (erFørstegangsbehandling) attesteringer?.any { attestering -> attestering.isUnderkjent() } else false,
                            kravtidspunkt = opprettet,
                            behandlingstype = if (erFørstegangsbehandling) FØRSTEGANGSBEHANDLING else SØKNAD,
                            fnr = Fnr.fromString(row.string("ident")),
                            saksnummer = row.stringOrNull("saksnummer")?.let { Saksnummer(it) },
                            id = id,
                            saksbehandler = saksbehandler,
                            beslutter = beslutter,
                            sakId = row.stringOrNull("sak_id")?.let { SakId.fromString(it) },
                        )
                    }.asList,
                ).let {
                    Saksoversikt(it)
                }
        }
}
