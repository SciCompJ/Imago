## Imago

Imago is a graphical image processing application written in Java, with a specific target to the processing and analysis of scientific images. 
The graphical user interface is similar to that of the popualt ImageJ software. The aim is to be able to manage multi-dimensional images of rather any type (values, multi-channels, spectra...).

Among the features:

* import image files in common formats, management of ImageJ TIFF files
* visualisation of 2D/3D grayscale, color or vector images, patial support of multi-channel images
* interactive exploration of image (line profile, histogram...)
* growing collection of image processing operators (linear or morphological filtering, segmentation, morphological reconstruction...)
* computation of region features from segmented images (area, perimeter, equivalent ellipse...)
* native support of data tables (import, export)
* more to come...

The Imago application is mostly based on the [CS4J library](https://github.com/SciCompJ/cs4j).

### Installation

The simplestway to install is to download a zip version (i.e. a "Imago-x.y.z.zip" file) from [one of the latest releases](https://github.com/SciCompJ/Imago/releases).

Unzip the archive, make sure a recent version (21+ required) of java is installed, open a terminal, and either 1) run the imago.bat script (Windows user) or run the following command:

    java -Xmx4g -Xms4g -cp Imago-0.1.3.jar;lib/cs4j-0.3.0.jar;lib/jcommon-1.0.17.jar;lib/xchart-3.8.0/xchart-3.8.0.jar;lib/jfreechart-1.0.14.jar;lib/gson-2.8.6.jar;. imago.Imago

(you may jave to adapt memory settings or dependency versions)
