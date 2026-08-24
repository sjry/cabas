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
3. **Courses** — la liste se calcule automatiquement, en deux présentations :
   *Total (A→Z)* avec les ingrédients fusionnés, ou *Par recette*. Les cases cochées
   sont conservées, y compris après un changement du nombre de convives.

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
Sur cette machine, le `java` du PATH est un JDK 11 que le plugin Android refuse ; en ligne
de commande, préfixer les appels Gradle :

```bash
JAVA_HOME=~/.jdks/jbr-21.0.11 ./gradlew assembleDebug
```

### Deux réglages liés à la machine

- `local.properties` (non versionné) donne le chemin du SDK Android.
- `gradle.properties` définit `kotlin.daemon.jvmargs` avec un `org.sqlite.tmpdir`
  personnalisé. Room vérifie ses requêtes SQL à la compilation via *sqlite-jdbc*,
  qui extrait une bibliothèque native dans `java.io.tmpdir` ; quand `/tmp` est monté
  `noexec`, la compilation échoue avec `No native library found for os.name=Linux`.
  Ce chemin est à adapter sur une autre machine.

### Build release signée

La configuration de signature est lue dans `keystore.properties` à la racine — **non
versionné**, comme la clé elle-même (`~/keystores/appli-cuisine.jks`). Si ce fichier est
absent, le projet compile quand même : la release sort simplement non signée.

```bash
./gradlew assembleRelease   # -> app/build/outputs/apk/release/app-release.apk
adb install app/build/outputs/apk/release/app-release.apk
```

La release passe par R8 (`isMinifyEnabled` + `isShrinkResources`), ce qui ramène l'APK
de 17 Mo à environ 1,5 Mo.

La clé garde son nom de fichier d'origine (`appli-cuisine.jks`) : une clé de signature est
un secret, pas un nom d'application, et la renommer n'apporterait rien. De même pour l'AVD.

> **À sauvegarder** : `~/keystores/appli-cuisine.jks` et `keystore.properties`. Cette clé
> est la seule qui permette de publier une mise à jour installable par-dessus l'app
> existante. Perdue, il faudrait désinstaller l'application avant de pouvoir en réinstaller
> une nouvelle version — et les données locales seraient effacées.

Un APK release et un APK debug ne peuvent pas cohabiter sur un même appareil : leurs
signatures diffèrent. Désinstaller l'un avant d'installer l'autre.

### Émulateur

Un AVD `cuisine_pixel6_api35` (Pixel 6, API 35, Google APIs x86_64) est configuré sur cette
machine. Depuis Android Studio : *Device Manager* puis le bouton ▶. En ligne de commande :

```bash
$ANDROID_HOME/emulator/emulator -avd cuisine_pixel6_api35 &
./gradlew installDebug
```

## Architecture

```
parser/     RecipeParser, IngredientUnit, QuantityFormatter  — Kotlin pur, testé
data/       Room (recettes, menu, articles cochés), ShoppingListBuilder, imports
ui/         Compose Material 3 : recettes, détail, menu, liste de courses
```

Les quantités sont stockées en unité canonique (grammes / millilitres) pour 1 personne ;
la multiplication par le nombre de convives et l'agrégation se font à l'affichage, dans
[`ShoppingListBuilder`](app/src/main/java/com/sjarry/cabas/data/ShoppingListBuilder.kt).

Les conventions de code, les règles de domaine à ne pas casser et la façon de vérifier une
modification sur l'émulateur sont réunies dans [`AGENTS.md`](AGENTS.md).
