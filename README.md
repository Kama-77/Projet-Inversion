# Kama's Inversion Mod

Kama's Inversion Mod est un mod **Fabric** pour **Minecraft 1.20.1**, écrit en **Java 21**.

Ce repo contient un squelette minimal de mod pour commencer rapidement.

## Prérequis

- Java 21 (JDK)
- Gradle installé sur ta machine (ou Gradle Wrapper si tu l'ajoutes plus tard)

## Importer dans ton IDE

1. Ouvre ce dossier (`kamas-inversion-mod`) dans ton IDE (IntelliJ, VS Code + extensions, Cursor, etc.).
2. Laisse l'IDE importer le projet Gradle automatiquement.

## Tâches utiles

- `gradle runClient` : lance Minecraft en mode développement avec le mod chargé.
- `gradle build` : construit le JAR de ton mod dans `build/libs`.

## Où commencer à coder

- Classe principale du mod : `src/main/java/com/kama/inversion/KamasInversionMod.java`
- Déclaration du mod : `src/main/resources/fabric.mod.json`
- Ressources (lang, textures, etc.) : `src/main/resources/assets/kamas_inversion_mod/`

