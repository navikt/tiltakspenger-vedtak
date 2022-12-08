## Hva skjedde?
tiltakspenger-vedtak var nede i 1 time etter at vi hentet et tiltak fra Arena der deltakelseperioden hadde en fra-dato som var _etter_ til-dato

## Hvilken effekt hadde det?
tiltakspenger-saksbehandler var utilgjengelig i dette tidsrommet og innkomne søknader ble midlertidig ikke håndtert

## Hvordan oppdaget vi feilen
Kom varsel på Slack, i #tpts-varsel-prod

## Rotårsak til feilen
Deltakelseperioden så ut som følger ([Link til fullstendig doc i Kibana](https://logs.adeo.no/app/discover#/doc/tjenestekall-*/tjenestekall-team-tpts-000002?id=-lZr8YQB9vbse6zjEL3A))

```
"deltakelsePeriode": {
   "fom": "2003-10-08",
   "tom": "2003-06-18"
}
```

Vi kikket på _gjennomføringen_ i Arena, og så at tom var satt til 2004-06-18. Ett år forskjell der, altså. Vi er i utgangspunktet ikke så interessert i så gamle aktiviteter, så vi filtrerer de bort før de lagres hos oss, men er litt bekymret for de vi ikke filtrerer bort. 

Via et skjermbilde forsøkte vi å legge inn en deltakelse i Arena med fom etter tom. Det gikk ikke. Så det er innført en validering der en eller annen gang. Når?

## Læring
Datakvalitet i Arena er ikke optimal.    
Datovalidering i skjermbildet i Arena er lagt til en eller annen gang etter 2003-04

## Hva gjorde vi bra
Oppdaget feilen tidlig siden vi får varsler i Slack   
Flere bidro for å rette   
Rullet fremover

## Hva gjorde vi dårlig
:shrug:

## Hvordan hindrer vi at noe lignende skjer igjen? (tiltak)
- dele tiltakspenger-vedtak i en api-del og en rivers-del. [Arbeid pågår](https://trello.com/c/gOk7H3FH/74-splitte-rest-og-rr-i-tiltakspenger-vedtak)
- sjekke med Arena om det er mulig å rette feilen: https://nav-it.slack.com/archives/C02FZ09S7T6/p1670503420897609
- innføre en mekanisme som sender dette til manuell behandling dersom det er et relevant tiltak. Avventer denne for å se hva svaret på ovenstående blir
