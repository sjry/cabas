# Notes pour les agents

Complément au [README](README.md), qui reste la référence sur le **format des recettes** et
l'usage de l'application. Ce fichier-ci décrit comment travailler dans le dépôt : commandes,
conventions, pièges de la machine, et façon de vérifier une modification d'interface.

## L'application en une phrase

Cabas est une application Android (Kotlin, Compose, Room) qui parse des recettes markdown
écrites **pour 1 personne**, laisse composer un menu avec un nombre de convives par recette,
et en déduit une liste de courses.

## Carte du dépôt

```
app/src/main/java/com/sjarry/cabas/
  parser/    RecipeParser, IngredientUnit, QuantityFormatter — Kotlin pur, sans Android, testé
  data/      Room (AppDatabase, entities/, dao/), RecipeRepository, MenuRepository,
             ShoppingListBuilder (logique pure), SettingsStore (DataStore)
  ui/        Navigation.kt + un dossier par écran (recipes, detail, menu, shopping),
             common/ pour les composables et le formatage partagés
app/src/test/            tests JUnit JVM (parseur, formatage, liste de courses)
app/schemas/             schémas Room exportés (2.json) — versionnés, à committer
exemples/                quatre recettes markdown d'exemple
```

## Commandes

```bash
JAVA_HOME=~/.jdks/jbr-21.0.11 ./gradlew test           # tests unitaires JVM
JAVA_HOME=~/.jdks/jbr-21.0.11 ./gradlew assembleDebug  # APK debug
JAVA_HOME=~/.jdks/jbr-21.0.11 ./gradlew installDebug   # installe sur l'appareil connecté
```

**Le `JAVA_HOME` n'est pas décoratif** : le `java` du PATH de cette machine est un JDK 11, et
le plugin Android exige 17+. Sans lui, la build échoue immédiatement avec
`Android Gradle plugin requires Java 17 to run`. Les JDK disponibles ici :
`~/.jdks/jbr-21.0.11` et `~/android-studio/jbr`.

## Ce que les tests couvrent — et ne couvrent pas

32 tests JUnit, tous en JVM pure : `RecipeParserTest`, `QuantityFormatterTest`,
`ShoppingListBuilderTest`. **Aucun test instrumenté** (`app/src/androidTest` n'existe pas,
la seule dépendance de test est `junit`). `./gradlew connectedAndroidTest` n'a donc rien à
exécuter : toute modification d'interface se vérifie à la main sur l'émulateur.

Toute logique nouvelle qui peut vivre dans `parser/` ou dans `ShoppingListBuilder` doit y
vivre, précisément pour rester testable sans appareil.

## Conventions

- **Tout est en français** : chaînes de l'interface, KDoc, commentaires, noms de tests.
  Le code (identifiants, types) reste en anglais.
- Les commentaires expliquent le **pourquoi**, pas le quoi. Voir `switchToTab` dans
  [Navigation.kt](app/src/main/java/com/sjarry/cabas/ui/Navigation.kt) ou la note sur
  `kotlin.daemon.jvmargs` dans `gradle.properties` : ce sont les modèles à suivre.
- Pas de framework d'injection. Les ViewModels sont construits dans
  [AppViewModelProvider](app/src/main/java/com/sjarry/cabas/ui/AppViewModelProvider.kt),
  les dépendances viennent de `AppContainer` (`CabasApplication`).
- Chaque écran expose un `StateFlow` d'état via `stateIn(..., WhileSubscribed(5_000), ...)`
  et se collecte avec `collectAsStateWithLifecycle()`.
- Material 3, thème dans `ui/theme/`. Les composables partagés (`EmptyState`…) sont dans
  `ui/common/Components.kt`, les libellés (`quantityLabel`, `servingsLabel`) dans
  `ui/common/Formatting.kt` — s'en servir plutôt que de reformater sur place.

## Règles du domaine à ne pas casser

- **Les quantités sont stockées pour 1 personne**, en unité canonique (grammes,
  millilitres). La multiplication par le nombre de convives se fait **à l'affichage** :
  dans `ShoppingListBuilder` pour la liste de courses, dans `RecipeDetailScreen` pour le
  détail d'une recette. Ne jamais écrire une quantité mise à l'échelle en base.
- Un ingrédient sans quantité (`unspecified`) n'est jamais multiplié : il s'affiche `qs`.
- Les cases cochées de la liste de courses sont liées à une clé stable
  (`ShoppingListBuilder.keyOf`, nom normalisé + unité) pour survivre au recalcul.
- Les trois onglets sont frères : tout passage de l'un à l'autre passe par
  `NavHostController.switchToTab`, jamais par un `navigate` direct.
- Le détail d'une recette prend un argument de navigation optionnel `servings`
  (`recipe/{recipeId}?servings=N`, 1 par défaut). Ouvert depuis le **Menu**, il reçoit les
  convives de la carte ; ouvert depuis **Recettes**, il reste à 1 personne.

## Modifier la base

`AppDatabase` est en `version = 2`, `exportSchema = true`. Un changement de schéma impose
de monter la version, d'ajouter une `Migration` (voir `MIGRATION_1_2`) et de committer le
nouveau JSON de `app/schemas/`. `fallbackToDestructiveMigration()` est actif : une migration
oubliée efface silencieusement les données de l'utilisateur au lieu de planter.

## Vérifier une modification sur l'émulateur

L'AVD `cuisine_pixel6_api35` est configuré sur cette machine.

```bash
export ANDROID_HOME=/home/sjarry/Android/Sdk        # = sdk.dir de local.properties
$ANDROID_HOME/emulator/emulator -avd cuisine_pixel6_api35 -no-snapshot-save -no-boot-anim &
$ANDROID_HOME/platform-tools/adb wait-for-device
$ANDROID_HOME/platform-tools/adb shell getprop sys.boot_completed   # attendre « 1 » (~20 s)
JAVA_HOME=~/.jdks/jbr-21.0.11 ./gradlew installDebug
adb shell monkey -p com.sjarry.cabas -c android.intent.category.LAUNCHER 1
```

**Peupler l'application sans passer par l'interface.** L'import réel ouvre un sélecteur de
fichiers (SAF) pénible à piloter en `adb shell input`. Il est bien plus simple d'écrire
directement dans la base — l'APK debug est `debuggable`, donc `run-as` fonctionne :

```bash
adb push seed.sql /data/local/tmp/seed.sql && adb shell chmod 666 /data/local/tmp/seed.sql
adb shell am force-stop com.sjarry.cabas   # sinon Room réécrit par-dessus depuis son cache
adb shell 'run-as com.sjarry.cabas sqlite3 databases/cabas.db < /data/local/tmp/seed.sql'
```

Le passage par un fichier poussé évite les cauchemars de quoting : `adb shell run-as ...
"INSERT ..."` fait interpréter les parenthèses et les points-virgules par le shell du
téléphone. Attention aussi : le client `sqlite3` n'active pas `PRAGMA foreign_keys`, donc
un `DELETE FROM recipes` laisse des `ingredients` orphelins qui viendront se rattacher au
prochain `recipeId` réutilisé.

> **Avant de semer des données, prévenir ou repartir propre.** Écrire dans `cabas.db`
> détruit les recettes déjà présentes sur l'émulateur. `adb shell pm clear com.sjarry.cabas`
> remet l'application à zéro de façon explicite ; c'est préférable à un `DELETE` partiel.

**Piloter et regarder l'écran.** `adb shell input tap X Y` en coordonnées appareil
(1080 × 2400 sur cet AVD). Une capture brute dépasse la limite de 2000 px de l'outil de
lecture d'images : la réduire d'abord (Pillow est disponible, pas ImageMagick).

```python
png = subprocess.run([adb, "exec-out", "screencap", "-p"], capture_output=True).stdout
im = Image.open(io.BytesIO(png)); im.thumbnail((820, 1820)); im.save(out)
```

Pour convertir une coordonnée lue sur la capture réduite vers l'appareil : multiplier par
`2400 / hauteur_de_la_capture` (≈ 1,32 avec les valeurs ci-dessus).

## Pièges de la machine

- `/tmp` est monté `noexec`. Room vérifie ses requêtes à la compilation via *sqlite-jdbc*,
  qui extrait une bibliothèque native dans `java.io.tmpdir` ; d'où le `org.sqlite.tmpdir`
  personnalisé dans `gradle.properties`. À adapter sur une autre machine, sans quoi la
  compilation échoue sur `No native library found for os.name=Linux`.
- `local.properties` (chemin du SDK), `keystore.properties` et `*.jks` ne sont **pas**
  versionnés et ne doivent jamais l'être.
- Sans `keystore.properties`, la build release compile mais sort non signée — c'est voulu.
- Un APK debug et un APK release ne cohabitent pas sur un même appareil : signatures
  différentes, il faut désinstaller l'un avant d'installer l'autre.

## Git

Branche `main`, historique très court. Ne pas committer ni pousser sans demande explicite.
Vérifier que `app/schemas/` suit bien quand le schéma bouge.

⚠️ `app/build/` **est suivi par git** (~2 560 fichiers) : le `.gitignore` ne contient que
`/build`, qui ne vise que la racine. Conséquence : toute compilation salit `git status` avec
des `.dex`, des `.jar` et des classes générées. Un `git add -A` embarquerait tout. En
attendant un `git rm -r --cached app/build` accompagné d'une ligne `app/build/` dans le
`.gitignore`, restreindre les commandes git aux chemins voulus (`git status --short -- app/src`,
`git add app/src ...`).
