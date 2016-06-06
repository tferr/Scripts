#!/bin/bash
## IJ BAR: https://github.com/tferr/Scripts#scripts
## Symlinks files in the repository to a Fiji installation
## TF 201606

## Source and destination paths
    SRC_DIR=`dirname $(pwd -P)`
    FIJI_HOME="/Applications/IJ/FijiDev.app"
    BAR_DIR="$FIJI_HOME/plugins/Scripts/BAR"

## Symlink lib/ files
    DIR="$BAR_DIR/lib"
    mkdir -p $DIR
    ln -fs $SRC_DIR/lib/* $DIR

## Symlink Analysis/ files
    DIR="$BAR_DIR/Analysis"
    mkdir -p $DIR
    ln -fs $SRC_DIR/Analysis/* $DIR

## Symlink Annotation/ files
    DIR="$BAR_DIR/Annotation"
    mkdir -p $DIR
    ln -fs $SRC_DIR/Annotation/* $DIR

## Symlink Data_Analysis/ files
    DIR="$BAR_DIR/Data_Analysis"
    mkdir -p $DIR
    ln -fs $SRC_DIR/Data_Analysis/* $DIR

## Symlink Segmentation/ files
    DIR="$BAR_DIR/Segmentation"
    mkdir -p $DIR
    ln -fs $SRC_DIR/Segmentation/* $DIR

## Symlink Snippets/ files
    DIR="$BAR_DIR/Snippets"
    mkdir -p $DIR
    ln -fs $SRC_DIR/Snippets/* $DIR

## Symlink Snippets/ files
    DIR="$BAR_DIR/lib"
    mkdir -p $DIR
    ln -fs $SRC_DIR/lib/* $DIR

## Symlink macro tools
    DIR="$FIJI_HOME/macros/tools"
    mkdir -p $DIR
    ln -fs $SRC_DIR/Tools/*.ijm $DIR

## Symlink macro toolsets
    DIR="$FIJI_HOME/macros/toolsets"
    mkdir -p $DIR
    ln -fs $SRC_DIR/Tools/Toolsets/*.ijm $DIR
