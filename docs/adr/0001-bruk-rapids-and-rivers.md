# Bruk Rapids & Rivers

* Status: Foreslått
* Besluttere: [https://github.com/haagenhasle](@haagenhasle), [https://github.com/tu55eladd](@tu55eladd), [https://github.com/richardmartinsen](@richardmartinsen), [https://github.com/pjwalstrom](@pjwalstrom)
* Dato: 2022-06-07

## Kontekst og problembeskrivelse

Å lage en vedtaksløsning i NAV krever samhandling med en rekke systemer.

## Beslutningsdrivere

* To punkter i NAVs ønskede tekniske retning er relevante 
    1. synkront mellom områder -> asynkront mellom områder
    2. integrere direkte -> publisere hendelser om det som har skjedd
* Vi skal løse samme problem som flere andre team har løst før oss - det er mye erfaring å høste derfra
* Det er mye kunnskap om "Rapids and Rivers" i NAV

## Vurderte alternativer

1. Rapids and Rivers, etter modell fra Dagpenger og Sykepenger
2. Kafka Streams, etter modell fra AAP
3. Synkrone tjenester med direktekoblinger fra vedtaksløsningen til eksterne systemer

## Beslutningsutfall

Valgt alternativ: Alternativ 1 fordi det er den mest brukte teknologien og vi vet den dekker alle behovene våre. 

Alternativ 3 strider mot NAVs tekniske målbilde og alternativ 2 er foreløpig lite brukt i produksjon

### Positive konsekvenser

* Vi bruker velprøvd arkitektur
* Vi løskobler systemer og bryter opp i vedlikeholdbare deler

### Negative konsekvenser

* Vi låser oss til NAVs egne biblioteker som ikke nødvendigvis vedlikeholdes over tid

## Links 

* [NAVs tekniske retning](https://app.mural.co/t/navdesign3580/m/navdesign3580/1644499953269/d0b9ec052c800f14a0e8c12c0933d8d1d8ceb54d)
* [Rapids and rivers fra NAV](https://github.com/navikt/rapids-and-rivers)
* [Rapids and rivers fra Fred George](https://vimeo.com/79866979)
* [Gently down the Stream - A gentle introduction to Apache Kafka](https://www.gentlydownthe.stream/)
