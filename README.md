## Imago

Imago is a graphical image processing application written in Java,
with a specific target to the processing and analysis of scientific images. 
The graphical user interface is similar to that of the popular ImageJ software.
The aim is to be able to manage in a user-friendly way images of any dimensionality 
(2D, 3D, timelapse...) and of a large variety of data type (grayscale, floating-points, 
multi-channels, spectra, label maps...).

![analyzing a grayscale image with Imago](images/imago-overview.png)

Among the features:

* Import image files in common formats, management of ImageJ TIFF files, import of "virtual" images (using file-memory mapping)
* Visualization of 2D/3D images, with adaptative display depending on image type (intensity, binary, label map, distance map...)
* Support for large variety of image data types: integer (8,16,32 bits), floating point, color. Partial support of vector and multi-channel images.
* Interactive exploration of images (line profile, histogram...)
* Basic editing of images (crop, type conversion, enhance contrast...)
* Growing collection of image processing operators (linear or morphological filtering, segmentation, morphological reconstruction...)
* Computation of 2D/3D region features from segmented images (area, perimeter, equivalent ellipse...)
* Native support of data tables (import, export)
* Several operators allow to generate a "view" on the result, instead of allocating memory for the whole image
* Possibility to include and develop user plugins
* More to come...

The Imago application is mostly based on the [CS4J library](https://github.com/SciCompJ/cs4j).

## Installation

The simplest way to install is to download a zip version (i.e. a "Imago-x.y.z.zip" file) 
from [one of the latest releases](https://github.com/SciCompJ/Imago/releases).

Unzip the archive, make sure a recent version (21+ required) of java is installed,
open a terminal, and either 1) run the imago.bat script (Windows user) or run the following command:

    java -Xmx4g -Xms4g -cp lib/jama-1.0.3.jar;lib/jcommon-1.0.17.jar;lib/xchart-3.8.8.jar;lib/gson-2.10.1.jar;lib/cs4j-0.4.4.jar;Imago-0.3.2.jar;. imago.Imago

(you may jave to adapt memory settings or dependency versions)

## Extensions (Plugins)

Imago can be extended by installing user plugins.
Several plugins are provided within the SciCompJ hub:

* [hierarchical-watershed](https://github.com/SciCompJ/hierarchical-watershed) A plugin for performing hierarchical watershed segmentation (the result is a valuation of the watershed boundaries)
* [homogenize-background](https://github.com/SciCompJ/homogenize-background) Make value of background more uniform by removing large-scale trends
* [region-features](https://github.com/SciCompJ/region-features) Adaptation of the RegionFeatures ImageJ plugin for the Imago software
