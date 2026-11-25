# AI_LOG: XP^AI radionica, tim Srećkovici

## Alati i razine

| Tko | Alat i model | Razina po Smjernicama |
|---|---|---|
| Svi, planiranje | Claude (claude.ai) | 3 |
| Par 1, Jan i Lucija | upisati alat i model | 3 |
| Par 2, Ennio i Marija | upisati alat i model | 3 |
| Par 3, Ana | Claude Code (Claude Opus 5.5) + Claude chat za provjeru | 3 za XP-3, 4 za XP-6 |

## Pravila

- Upit i odluku upisujemo odmah, a ne na kraju radionice.
- U upite ne lijepimo `.env`, API ključeve, lozinke ni stvarne podatke trgovaca.
- Ništa što AI napiše ne ide u commit dok test ne prođe i dok ga par ne pročita i ne zna objasniti.
- Kad AI pogriješi, upisujemo što je pogriješio i kako smo to otkrili.

## Zapisi

| Min | Par | Priča | Razina | Upit (sažeto) | Što je AI dao | Što smo napravili s tim i zašto |
|---|---|---|---|---|---|---|
| 0 | Jan | Plan | 3 | Claude je dobio prezentaciju te predložiti raspodjelu posla | Ennio + Marija: xp-radionica/postavke-pristupacnosti, u 2. iteraciji xp-radionica/veci-tekst
Ana: xp-radionica/opisi-za-citac-zaslona, u 2. iteraciji xp-radionica/redak-transakcije-citac |Prihvatili smo podjelu posla i napravili kostur kako bi se moglo početi s radom |
| 39 | Ana | XP-3 | 3 | Pronađi ikone bez opisa, klikabilne bez teksta i naslove bez heading() u ui/users i ui/shell; ništa ne mijenjaj | Popis: 6 ukrasnih ikona (ispravno), SaveButton i UserRow bez oznake, 3 naslova bez heading(), 2 spinnera bez opisa | Prihvatili stavke 1–4, odbili opcionalne i bočne napomene (izvan priče). Sve nalaze ručno provjerili u kodu, točni su |
| 43 | Ana | XP-3 | 3 | Napiši samo 6 Compose UI testova iz kriterija, bez koda | UsersScreenReaderTest, 6 testova | Pokrenuli na emulatoru: 6/6 crveno na assertu (pravi razlog). Commit RED lokalno |
| 56 | Ana | XP-3 | 3 | GREEN: najmanja promjena za stavke 1–4, ne diraj testove | heading(), onClickLabel, contentDescription u 5 datoteka + 2 stringa | 6/6 zeleno, pregledali diff, push i PR |

## Gdje je AI pogriješio

| Min | Par | Što je AI predložio | Kako smo otkrili da ne valja | Kako smo ispravili |
|---|---|---|---|---|
| 43 | Ana | Da test i kod idu u isti commit, bez RED commita | XP pravilo traži vidljiv RED commit u povijesti | Odbili, napravili zaseban RED commit 0c68fc0 |
| 39 | Ana | Nije bio siguran označava li M3 top bar naslov kao heading (sam je to naveo) | Test XP3_form_top_bar_title_is_heading je pao | Pretpostavka provjerena testom, izmjena ostala |

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
