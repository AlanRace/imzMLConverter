# imzMLConverter #

Tool for converting mass spectrometry data to the [imzML](http://imzml.org) format. 


If you find this useful please cite:  Alan M. Race, Iain B. Styles, Josephine Bunch, *Journal of Proteomics*, 75(16):5111-5112, 2012. http://dx.doi.org/10.1016/j.jprot.2012.05.035

### How do I get set up? ###

* Download the [latest version](https://github.com/AlanRace/imzMLConverter/releases) of imzMLConverter
* Install [Java SE 8 JRE](https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
* Install [ProteoWizard](http://proteowizard.sourceforge.net/)
* [Optional] Install [MS Data Converter (SCIEX)](https://sciex.com/sw-downloads-form?d=ab_sciex_ms_data_converter_V1.3%20beta.zip&asset=software&softwareProduct=MS%20Data%20Converter%20(Beta%20Version%201.3)) to enable conversion of data from AB SCIEX data instruments

### Converting data ###

imzMLConverter is currently a command line tool. To use this tool, open the folder where `jimzMLConverter-2.0.2.jar` is stored, hold the shift button and right click in the folder. Select `Open command window here` from the drop-down menu.

To see a full list of options for the converter run the command:

`java -jar jimzMLConverter-2.0.2.jar`

#### Converting Waters data
* Modify the following command:

`java -jar jimzMLConverter-2.0.2.jar imzML -p "C:\Path\To\Data\my_data.pat" "C:\Path\To\Data\my_data.raw"`

#### Converting ION-TOF data

* Export the data to GRD, ensuring that the properties.txt file is generated
* Modify the following command:

`java -jar jimzMLConverter-2.0.2.jar imzML -p "C:\Path\To\Data\my_data.properties.txt" "C:\Path\To\Data\my_data.grd"`
