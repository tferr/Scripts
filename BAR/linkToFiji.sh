#!/bin/bash
## Symlinks files in the repository to a Fiji installation
## TF 2014.06

## Source and destination paths
    SRC_DIR=`dirname $(pwd -P)`
    FIJI_HOME="/Applications/Fiji/Fiji.app"
    BAR_DIR="$FIJI_HOME/plugins/Scripts/BAR"


## 1. Symlink Analysis/ files (create $BAR_DIR if needed)
    DIR="$BAR_DIR/Analysis"
    if [ ! -d $DIR ]; then
        mkdir -p $DIR
    fi
    ln -f -s $SRC_DIR/Analysis/* $DIR

## 2. Symlink Annotation/ files
    DIR="$BAR_DIR/Annotation"
    if [ ! -d $DIR ]; then
        mkdir $DIR
    fi
    ln -f -s $SRC_DIR/Annotation/* $DIR

## 3. Symlink Morphometry/ files
    DIR="$BAR_DIR/Morphometry"
    if [ ! -d $DIR ]; then
        mkdir $DIR
    fi
    ln -f -s $SRC_DIR/Morphometry/* $DIR

## 4. Symlink Segmentation/ files
    DIR="$BAR_DIR/Segmentation"
    if [ ! -d $DIR ]; then
        mkdir $DIR
    fi
    ln -f -s $SRC_DIR/Segmentation/* $DIR

## 5. Symlink installers
    DIR="$BAR_DIR/Tool_Installers"
    if [ ! -d $DIR ]; then
        mkdir $DIR
    fi
    ln -f -s $SRC_DIR/BAR/installers/* $DIR

## 6. Symlink macro tools
    DIR="$FIJI_HOME/macros/tools"
    if [ ! -d $DIR ]; then
        mkdir $DIR
    fi
    ln -f -s $SRC_DIR/Tools/*.ijm $DIR

## 7. Symlink macro toolsets
    DIR="$FIJI_HOME/macros/toolsets"
    if [ ! -d $DIR ]; then
        mkdir $DIR
    fi
    ln -f -s $SRC_DIR/Tools/Toolsets/*.ijm $DIR
