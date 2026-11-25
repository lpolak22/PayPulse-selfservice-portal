# AI_LOG: XP^AI radionica, tim Srećkovici

## Alati i razine

| Tko | Alat i model | Razina po Smjernicama |
|---|---|---|
| Svi, planiranje | Claude (claude.ai) | 3 |
| Par 1, Jan i Lucija | Claude Code (Claude Opus 5.5) | 3 |
| Par 2, Ennio i Marija | upisati alat i model | 3 |
| Par 3, Ana | upisati alat i model | 3 za XP-3, 4 za XP-6 |

## Pravila

- Upit i odluku upisujemo odmah, a ne na kraju radionice.
- U upite ne lijepimo `.env`, API ključeve, lozinke ni stvarne podatke trgovaca.
- Ništa što AI napiše ne ide u commit dok test ne prođe i dok ga par ne pročita i ne zna objasniti.
- Kad AI pogriješi, upisujemo što je pogriješio i kako smo to otkrili.
- AI ne smije mijenjati testove. Testovi su ugovor, a AI piše samo produkcijski kod.
- Kad AI tvrdi neku brojku (npr. omjer kontrasta), ne vjerujemo na riječ: provjerava je test, a razliku upisujemo u log.

## Zapisi

| Min | Par | Priča | Razina | Upit (sažeto) | Što je AI dao | Što smo napravili s tim i zašto |
|---|---|---|---|---|---|---|
| 0 | Jan | Plan | 3 | Claude je dobio prezentaciju te predložiti raspodjelu posla | Ennio + Marija: xp-radionica/postavke-pristupacnosti, u 2. iteraciji xp-radionica/veci-tekst<br>Ana: xp-radionica/opisi-za-citac-zaslona, u 2. iteraciji xp-radionica/redak-transakcije-citac | Prihvatili smo podjelu posla i napravili kostur kako bi se moglo početi s radom |
| __ | Par 1 (vozila Lucija) | Kostur | 3 | Claude Code: pripremi kostur iz zipa na `xp-radionica/dev`, ali ništa ne commitaj | Kopirao 7 datoteka kostura, pokrenuo build i unit testove: 169/169 zeleno, izgled aplikacije isti jer su boje samo preseljene u `ColorSchemes.kt` | Pregledali smo datoteke i sami commitali u dva commita, kako piše u planu |
| __ | Par 1 (vozila Lucija) | Kostur | 3 | Push iz GitSwampa odbijen: „refusing to allow an OAuth App to create or update workflow ... without `workflow` scope” | Objasnio da GitSwamp nema dopuštenje `workflow` za `.github/workflows/`, upozorio da ne radimo force push i predložio `git push origin xp-radionica/dev` iz terminala | Push iz terminala je prošao, workflow „XP radionica” se pokrenuo na GitHub Actions |
| __ | Par 1 (vozila Lucija) | Plan rada | 3 | Kako raditi s AI-jem da poštujemo razinu 3 i da tim zna obraniti kod | Predložio da AI prvo objasni formulu i predvidi brojke, tek onda piše kod; da test provjerava svaku brojku koju AI tvrdi; i da na kraju AI postavi pitanja, a mi odgovorimo sami | Prihvatili smo taj ritam za XP-1 i dodali dva pravila u Pravila |
| __ | Par 1 (vozila Lucija) | XP-1 | 3 | Claude Code: napravi granu `xp-radionica/visoki-kontrast` iz `xp-radionica/dev` i kopiraj testove prihvatljivosti za XP-1 | Granu i 4 datoteke: `ContrastRatioTest` (8), `PayPulseColorSchemeTest` (6), `HighContrastThemeTest` (3, emulator) i prazan `contrastRatio` s `TODO("XP-1")`. Potvrdio da se sve prevodi i da 14 novih testova pada, a ostalih 169 prolazi | Testove smo commitali prije koda (crveno). Jan preuzima kao vozač za `contrastRatio`, Lucija je navigator |
| | | | | | | |

## Gdje je AI pogriješio

| Min | Par | Što je AI predložio | Kako smo otkrili da ne valja | Kako smo ispravili |
|---|---|---|---|---|
| | | | | |

## Priča na razini 4: XP-6

1. Kriteriji koje smo dali agentu:
2. Testovi koje je agent napisao prvi, i jesu li bili crveni prije koda:
3. Kod koji je agent napisao:
4. Naša analiza: što smo provjerili, što smo promijenili, rizici:
5. Objašnjenje koda vlastitim riječima (Ana):

## Zaključak tima

- Gdje nam je AI najviše pomogao:
- Gdje nam je najviše odmogao:
- Što bismo sljedeći put napravili drugačije:
