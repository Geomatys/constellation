#!/bin/sh

#
# Définit les variables utilisées par tous les scripts du répertoire ../bin/.
# Vous pouvez éditer ce fichier en fonction de la configuration de votre système.
# Ces variables seront locales à l'exécution des scripts; elles n'affecteront pas
# le système en dehors de ces scripts.
#

# Version des bibliothèques à utiliser par défaut
export GEOTOOLS_VERSION=2.5-SNAPSHOT
export CONSTELLATION_VERSION=1.0-SNAPSHOT

# Répertoire contenant l'ensemble des fichiers JARS.
export JARS="$BASE_DIR/jar"

# Options communes à tous les programmes Java exécutés par les scripts.
export OPTS="-Djava.util.logging.config.file=$BASE_DIR/etc/logging.properties"

