# Cabas

Application Android qui transforme des recettes markdown en liste de courses.

Les recettes sont écrites **en dehors** de l'application (éditeur de texte, Obsidian, Drive…),
importées puis stockées localement. L'utilisateur compose un menu en choisissant des recettes
et le nombre de convives ; l'application calcule la liste de courses correspondante.

## Format des recettes

Un fichier `.md` par recette, **toujours écrite pour 1 personne**. L'application multiplie
ensuite par le nombre de convives indiqué dans le menu.

```markdown
# Poulet au curry

## Ingrédients
- 150 g riz basmati
- 1 blanc de poulet
- 20 cl lait de coco
- 1/2 oignon
- 1 cac curry
- 1 gousse d'ail
- sel

## Étapes
1. Émincer l'oignon et l'ail, les faire revenir dans un filet d'huile.
2. Ajouter le blanc de poulet coupé en dés et le saisir 5 minutes.
3. Verser le lait de coco et le curry, saler, laisser mijoter 15 minutes.
```

Quatre exemples complets se trouvent dans [`exemples/`](exemples/).

### Structure

| Partie | Écriture |
|---|---|
| **Titre** | la première ligne `#` du fichier. À défaut, le nom du fichier est utilisé |
| **Ingrédients** | un titre `##` contenant `ingr` (`## Ingrédients`, `## Ingredients`), puis une puce `-`, `*` ou `+` par ingrédient |
| **Étapes** | un titre `##` contenant `étape`, `préparation` ou `instruction`, puis une liste numérotée ou à puces |

Les accents et la casse des titres de section n'ont pas d'importance. Toute autre section
(`## Notes`, `## Astuces`…) est simplement ignorée.

### Ligne d'ingrédient

```
- <quantité> <unité> <nom>
```

**Quantité** — entier (`150`), décimal (`1.5` ou `1,5`), fraction (`1/2`), nombre mixte (`1 1/2`).
Si la quantité est absente (`- sel`), l'ingrédient est repris tel quel sur la liste, sans être
multiplié : il s'affiche `qs` (quantité suffisante).

**Unités reconnues** :

| Type | Écritures acceptées | Stockage |
|---|---|---|
| Masse | `g`, `gr`, `gramme(s)`, `kg`, `kilo(s)` | grammes |
| Volume | `ml`, `cl`, `dl`, `l`, `L`, `litre(s)` | millilitres |
| Unité | *aucune unité écrite* — `1 blanc de poulet`, `2 oeufs` | pièces |
| Libre | `cac`, `cas`, `cuillère`, `pincée`, `gousse`, `sachet`, `botte`, `brin`, `branche`, `feuille`, `tranche`, `boîte`, `verre`, `paquet`, `bouquet`, `poignée` | telles quelles |

Les masses et les volumes sont convertis dans une unité commune, ce qui permet d'additionner
`250 g` et `0,5 kg`. À l'affichage, l'application choisit le multiple le plus lisible :
`1250 g` devient `1,25 kg`, `800 ml` devient `80 cl`.

Les cuillères s'écrivent `cac` et `cas`. Les écritures longues sont aussi acceptées à la
lecture — `c. à café`, `c.a.c.`, `cuillère à café`, et leurs équivalents pour la soupe — et
sont ramenées à `cac` / `cas` à l'affichage.

Les unités libres ne sont additionnées qu'entre elles : `2 cas` + `1 cas` = `3 cas`,
mais une cuillère ne sera jamais convertie en millilitres.

**Détails pratiques** :

- le mot de liaison est facultatif : `100 g de farine` et `100 g farine` donnent le même résultat ;
- le gras et l'italique sont retirés du nom : `100 g de **farine** T55` → `farine T55` ;
- un ingrédient suivi de `(optionnel)` ou `+ optionnel` est importé sans cette mention ;
- une ligne incompréhensible n'est jamais perdue : elle est conservée et signalée après l'import.

## Utilisation

1. **Recettes** — bouton *Importer* : choisir des fichiers `.md`, synchroniser un dossier
   entier, ou coller du markdown. Un dossier synchronisé est mémorisé : le bouton
   *Re-synchroniser* réimporte les fichiers modifiés et retire ceux qui ont disparu.
2. **Menu** — ajouter des recettes et régler le nombre de personnes pour chacune.
   Une case à cocher par recette permet de suivre ce qui a déjà été cuisiné : la recette
   cochée est estompée mais reste dans le menu, et ses ingrédients restent dans la liste
   de courses. Un appui sur la carte ouvre le détail de la recette, dont les quantités
   sont ajustées au nombre de convives choisi (la même recette ouverte depuis l'onglet
   *Recettes* reste affichée pour 1 personne).

   Le bouton *Ajouter* ouvre un écran de sélection pensé pour une grande bibliothèque :

   - une **recherche** sur les titres **et sur les ingrédients** — taper `coco` remonte
     les recettes au lait de coco, avec en sous-titre l'ingrédient qui les a fait sortir.
     Plusieurs mots se cumulent (`curry poulet`), les accents et la casse sont ignorés ;
   - sans recherche, la liste est **jalonnée par initiale** — `Éclair` se range à la
     lettre *E*, et les titres qui ne commencent pas par une lettre finissent sous `#` ;
   - une **barre de sélection** en bas retient les recettes cochées sous forme de puces,
     visibles où qu'on soit dans la liste et retirables d'un appui sur leur croix ;
   - les recettes déjà au menu n'y figurent pas.

   Pour les soirs sans idée, le bouton **dé** à côté d'*Ajouter* — ou le lien *Ou tirer au
   sort* quand le menu est vide — **tire des recettes au hasard**. On choisit combien :
   le compteur ne dépasse jamais le nombre de recettes hors menu, qu'il affiche, si bien
   qu'un tirage rend toujours exactement ce qu'on a demandé. Un second compteur donne le
   **nombre de personnes**, qui s'applique à *toutes* les recettes du tirage — on cuisine pour
   la même table — et reste ajustable ensuite carte par carte. Une recette déjà au menu n'est
   jamais retirée au sort. Quand tout est déjà au menu, le dé le dit et ne tire rien.

   Chaque carte porte aussi son **propre dé**, qui remplace cette recette-là par une autre,
   tirée au sort, **sans confirmation** : le créneau garde son nombre de convives et sa place
   dans la liste, seule la recette change — et la case *faite* repart à zéro, elle parlait de
   l'autre plat. De quoi relancer une carte qui ne plaît pas, autant de fois qu'il faut. Le dé
   d'une carte est grisé quand toutes les recettes importées sont déjà au menu : il n'y a plus
   rien à mettre à la place.
3. **Courses** — la liste se calcule automatiquement, en trois présentations :
   *Rayon* (par défaut), *A→Z* avec les ingrédients fusionnés, ou *Recette*. Les cases
   cochées sont conservées, y compris après un changement du nombre de convives.

   En vue *Rayon*, les articles sont groupés dans l'ordre du parcours en magasin :
   Fruits & légumes, Boucherie & poissonnerie, Crémerie, Boulangerie, Épicerie,
   Surgelés, Divers. Le rayon est deviné à partir du **nom de l'ingrédient** : rien à
   écrire dans les recettes. Un **appui long** sur un article corrige son rayon ; la
   correction est retenue et vaut pour toutes les recettes, et *Rayon automatique* la
   retire. Un appui court sur la ligne coche l'article.

   En vue *Rayon* et *A→Z*, un article coché quitte la liste principale et rejoint la
   section **Pris (n)** en bas de l'écran, dépliée par défaut : ce qu'il reste à prendre
   passe devant, sans perdre de vue ce qui est déjà dans le cabas. La section se replie
   d'un appui, et décocher un article le remet dans la liste. Quand plus rien ne reste,
   la liste est remplacée par **« Tout est dans le cabas »** et un bouton *Tout décocher*.

   La vue *Recette* répond à une autre question — « que demande ce plat ? » — et garde
   donc chaque ingrédient sous sa recette, simplement barré une fois pris : ni section
   *Pris*, ni écran de fin, même quand tout est coché.

Tout est stocké localement sur le téléphone (Room / SQLite). Aucune connexion réseau n'est utilisée.

## Compiler et lancer

Le projet s'ouvre directement dans **Android Studio** (`File > Open` sur ce dossier),
ou se compile en ligne de commande :

```bash
./gradlew test           # tests unitaires du parseur et du calcul de liste
./gradlew assembleDebug  # APK de debug -> app/build/outputs/apk/debug/
./gradlew installDebug   # installation sur un appareil connecté
```

Les tests sont des tests JVM purs (parseur, formatage, calcul de liste) : il n'y a pas de
test instrumenté, l'interface se vérifie à la main sur l'émulateur ou un téléphone.

Prérequis : JDK 17 ou 21, `compileSdk 35`, `minSdk 26` (Android 8.0).
Gradle 8.9 ne fonctionne pas avec un JDK 25 : si Android Studio propose son JBR embarqué,
choisir plutôt un JDK 21 dans *Settings > Build Tools > Gradle > Gradle JDK*.
En ligne de commande, si le `java` du PATH est antérieur à 17, le plugin Android refuse de
se charger (`Android Gradle plugin requires Java 17 to run`) : préfixer alors les appels
Gradle par le JDK voulu.

```bash
JAVA_HOME=/chemin/vers/jdk-21 ./gradlew assembleDebug
```

### Réglages propres au poste

- `local.properties` (non versionné) donne le chemin du SDK Android. Android Studio le crée
  à l'ouverture du projet ; en ligne de commande, exporter `ANDROID_HOME` suffit.
- Si la compilation échoue sur `No native library found for os.name=Linux`, c'est que `/tmp`
  est monté `noexec` : Room vérifie ses requêtes SQL à la compilation via *sqlite-jdbc*, qui
  a besoin d'extraire une bibliothèque native dans `java.io.tmpdir`. Donner alors un
  répertoire temporaire exécutable au démon Kotlin, dans `~/.gradle/gradle.properties`
  plutôt que dans le dépôt — le chemin ne vaut que pour ce poste :

  ```properties
  kotlin.daemon.jvmargs=-Xmx2048m -Dorg.sqlite.tmpdir=/chemin/exécutable -Djava.io.tmpdir=/chemin/exécutable
  ```

### Build release signée

La configuration de signature est lue dans `keystore.properties` à la racine — **non
versionné**, comme la clé elle-même (`cabas-release.jks`, également à la racine et ignorée
par `.gitignore`). Si ce fichier est absent, le projet compile quand même : la release sort
simplement non signée, donc non installable.

```bash
./gradlew assembleRelease   # -> app/build/outputs/apk/release/app-release.apk
adb install app/build/outputs/apk/release/app-release.apk
```

La release passe par R8 (`isMinifyEnabled` + `isShrinkResources`), ce qui ramène l'APK
de 17 Mo à environ 1,5 Mo.

La clé est une RSA 4096 valide 30 ans, sans rapport avec le nom de l'application : une clé
de signature est un secret, la renommer n'apporterait rien.

> **À sauvegarder** : `cabas-release.jks` et `keystore.properties`. Cette clé
> est la seule qui permette de publier une mise à jour installable par-dessus l'app
> existante. Perdue, il faudrait désinstaller l'application avant de pouvoir en réinstaller
> une nouvelle version — et les données locales seraient effacées.

Un APK release et un APK debug ne peuvent pas cohabiter sur un même appareil : leurs
signatures diffèrent. Désinstaller l'un avant d'installer l'autre.

### Installer sur un téléphone depuis les releases GitHub

Chaque tag `v*` poussé sur le dépôt déclenche le workflow
[`.github/workflows/release.yml`](.github/workflows/release.yml) : GitHub Actions lance les
tests, construit l'APK release **signé** et le publie dans une release. Depuis le téléphone,
il n'y a donc rien à brancher — ouvrir dans le navigateur :

**<https://github.com/sjry/cabas/releases/latest>**

puis toucher le fichier `cabas-<version>.apk` et l'ouvrir une fois téléchargé. Android
demande la première fois d'autoriser l'installation depuis le navigateur
(*Installer des applications inconnues*). Cette URL ne change jamais : elle pointe toujours
vers la dernière version publiée, ce qui la rend facile à mettre en favori ou en QR code.

#### Publier une nouvelle version

```bash
git tag v1.1 && git push origin v1.1
```

Le `versionName` est déduit du tag (`v1.1` → `1.1`) et le `versionCode` est le numéro de
build GitHub, donc strictement croissant : les mises à jour s'installent par-dessus la
précédente sans perdre les données. Le workflow se lance aussi à la main depuis l'onglet
*Actions* (la release s'appelle alors `v0.0.0-build<n>`), utile pour tester la chaîne sans
consommer un numéro de version.

#### Secrets à configurer une fois

La clé de signature n'est pas versionnée : le workflow la reconstitue depuis les secrets de
l'environment **`prod`** (*Settings > Environments > prod > Environment secrets*). Le job
déclare `environment: prod` — sans cette déclaration, GitHub n'injecterait que les secrets
du dépôt et la build échouerait sur `Secret KEYSTORE_BASE64 absent`. Cet échec est
volontaire : mieux vaut un message clair qu'un APK non signé, donc non installable.

| Secret | Contenu |
|---|---|
| `KEYSTORE_BASE64` | le `.jks` encodé : `base64 -w0 <clé>.jks` |
| `KEYSTORE_PASSWORD` | `storePassword` de `keystore.properties` |
| `KEY_ALIAS` | `keyAlias` |
| `KEY_PASSWORD` | `keyPassword` |

### Émulateur

L'application a été mise au point sur un AVD Pixel 6, API 35, Google APIs x86_64.
Le créer depuis Android Studio (*Device Manager*), puis le lancer par le bouton ▶ ou en
ligne de commande :

```bash
$ANDROID_HOME/emulator/emulator -list-avds
$ANDROID_HOME/emulator/emulator -avd <nom-de-l-avd> &
./gradlew installDebug
```

## Architecture

```
parser/     RecipeParser, IngredientUnit, IngredientCategory, QuantityFormatter
            — Kotlin pur, testé
data/       Room (recettes, menu, articles cochés, rayons corrigés),
            ShoppingListBuilder, imports
ui/         Compose Material 3 : recettes, détail, menu, liste de courses
```

Les quantités sont stockées en unité canonique (grammes / millilitres) pour 1 personne ;
la multiplication par le nombre de convives et l'agrégation se font à l'affichage, dans
[`ShoppingListBuilder`](app/src/main/java/com/sjarry/cabas/data/ShoppingListBuilder.kt).

Les conventions de code, les règles de domaine à ne pas casser et la façon de vérifier une
modification sur l'émulateur sont réunies dans [`AGENTS.md`](AGENTS.md).

## Hello English people

This app is about cooking so obviously this is in French! At this point i don't know if it's worth translating everthing. Or adding an option to change langage. But anyone who wants to do it will be welcome.