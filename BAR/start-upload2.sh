#!/bin/bash
## IJ BAR: https://github.com/tferr/Scripts#scripts
## Places scripts (symlinks) and latest jars in a local Fiji installation
## so that the ImageJ updater can upload all files to the BAR Update site
## TF 201611

## Source and destination paths
PROJECT_DIR=`dirname $(pwd -P)`
FIJI_DIR="/Applications/IJ/FijiUploadToBAR2.app"
FIJI_BAR_DIR="$FIJI_DIR/plugins/Scripts/BAR"
IJEXEC=$FIJI_DIR/Contents/MacOS/ImageJ-macosx


## Symlink files to be placed under BAR> submenus
for subdir in lib Snippets; do
    dir="$PROJECT_DIR/$subdir"
    echo "Symlinking "$dir
    ln -fs $dir $FIJI_BAR_DIR
done

## Symlink macro tools
dest_dir="$FIJI_DIR/macros/tools"
echo "Symlinking "$PROJECT_DIR/Tools/
ln -fs $PROJECT_DIR/Tools/*.ijm $dest_dir

## Symlink macro toolsets
dest_dir="$FIJI_DIR/macros/toolsets"
echo "Symlinking "$PROJECT_DIR/Tools/Toolsets/
ln -fs $PROJECT_DIR/Tools/Toolsets/*.ijm $dest_dir

## Remove old artifacts
echo "Deleting old artifacts..."
dest_dir="$FIJI_DIR/plugins"
rm $dest_dir/BAR_-*.jar

## Get latest artifact from jenkins and place it in ./plugins
##1. Read version from local pom
version=`mvn -o org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\['`
mvn_url="http://maven.imagej.net/service/local/repositories/releases/content/com/github/tferr/BAR_/"

##2. Download artifact using a permalink to its latest version
echo "Getting artifact $version from jenkins"
curl -o $dest_dir/BAR_-$version.jar $mvn_url$version/BAR_-$version.jar

# Run updater.
echo "Done. Running updater"

$IJEXEC --no-splash --update
