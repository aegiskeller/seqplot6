<h1>Seqplot Revisited</h1>
<h2>What is this?</h2>

It is an evolved version of seqplot5.7.1 from AAVSO Sequence Team. It is used to construct sequences of calibration stars around variable stars of interest.

<h2>Why a new version?</h2>

Previous methods have relied on manul processes of copying rows from a CDS Vizier search, dropping into a spreadsheet to perform the photometric transformations and then copying the results to a .txt file for submission to VSD. This manual process can be frustrating and raises the opportunity of errors. 

The new tool bakes in the rules for sequence construction and photometric transformation. It aims to expedite the boring stuff and allow the sequencer to concentrate on the skillful choice of comparison stars.

<h2>Request Star Page</h2>
![seqplot6_1](https://github.com/user-attachments/assets/daf4e54b-01b4-45d4-8b7e-3bbfc2b89ae2)

<h2>How do I start it?</h2>

It is Java so should run 'everywhere'. Included in the download is run-seqplot.bat file which is an attempt to wrap things so that one doesn't have to worry about paths etc. Give it a go by double clicking it. If it fails keep reading.

If run-seqplot.bat has failed chances are Java is not installed or is installed but not on the path. It is very unlikely to be a Java version issue as the program is compatible all the way back to Java 8 (2014).

Download Java: Visit https://adoptium.net/ and download the latest LTS version of OpenJDK. The website reads "The Power of Eclipse Temurin®
Download Temurin 25 for Windows x64" (Java has a propriety form, this is the open source version).

Install Java: Run the installer and make sure to check the option to "Add to PATH" during installation (I believe it is default)
Verify Installation: Open a new PowerShell window and run java -version

Run Seqplot: Once Java is installed, you can simply double-click the run-seqplot.bat file or run it from the command line

Fallback: yes one can run the jar. Use the full path to java.exe, for example:
"C:\Program Files\Java\jdk-17\bin\java.exe" -jar seqplot-v6.0.0-java8-standalone.jar
